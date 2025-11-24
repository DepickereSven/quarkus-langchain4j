package io.quarkiverse.langchain4j.agentic.deployment.validation;


import org.assertj.core.api.Assertions;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkus.test.QuarkusUnitTest;

public class InvalidOutputKeyWorkflowTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class))
            .assertException(
                    throwable -> Assertions.assertThat(throwable).isInstanceOf(IllegalConfigurationException.class)
                            .hasMessageContaining("No agent provides an output key named 'story'"));

    @Test
    public void test() {
        StoryCreatorWorkflow storyCreatorWorkflow = AgenticServices
                .sequenceBuilder(StoryCreatorWorkflow.class)
                .subAgents(createCreativeWriteAgent(), createAudienceEditorAgent(), createStyleEditorAgent())
                .outputKey("story-final")
                .build();

        storyCreatorWorkflow.write("topic", "style", "audience");
    }

    public CreativeWriterWorkflow createCreativeWriteAgent() {
        return AgenticServices.agentBuilder(CreativeWriterWorkflow.class).outputKey("story-initial").build();
    }

    public AudienceEditorWorkflow createAudienceEditorAgent() {
        return AgenticServices.agentBuilder(AudienceEditorWorkflow.class).outputKey("story-edited").build();
    }

    public StyleEditorWorkflow createStyleEditorAgent() {
        return AgenticServices.agentBuilder(StyleEditorWorkflow.class).outputKey("story-final").build();
    }


    public interface StoryCreatorWorkflow {

        @Agent
        String write(@V("topic") String topic, @V("style") String style, @V("audience") String audience);

    }

    public interface CreativeWriterWorkflow {

        @UserMessage("""
                You are a creative writer.
                Generate a draft of a story long no more than 3 sentence around the given topic.
                Return only the story and nothing else.
                The topic is {{topic}}.
                """)
        @Agent("Generate a story based on the given topic")
        String generateStory(@V("topic") String topic);
    }

    public interface AudienceEditorWorkflow {

        @UserMessage("""
                You are a professional editor.
                Analyze and rewrite the following story to better align with the target audience of {{audience}}.
                Return only the story and nothing else.
                The story is "{{story}}".
                """)
        @Agent("Edit a story to better fit a given audience")
        String editStory(@V("story") String story, @V("audience") String audience);
    }

    public interface StyleEditorWorkflow {

        @UserMessage("""
                You are a professional editor.
                Analyze and rewrite the following story to better fit and be more coherent with the {{style}} style.
                Return only the story and nothing else.
                The story is "{{story}}".
                """)
        @Agent("Edit a story to better fit a given style")
        String editStory(@V("story") String story, @V("style") String style);
    }

}
