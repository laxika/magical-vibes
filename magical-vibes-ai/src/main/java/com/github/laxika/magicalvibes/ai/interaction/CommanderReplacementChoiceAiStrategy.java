package com.github.laxika.magicalvibes.ai.interaction;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
class CommanderReplacementChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.CommanderReplacementChoice> {
    public Class<PendingInteraction.CommanderReplacementChoice> handledType() { return PendingInteraction.CommanderReplacementChoice.class; }
    public void answer(PendingInteraction.CommanderReplacementChoice choice, AiInteractionContext ctx) throws Exception {
        ctx.gameActions().answerInteraction(new InteractionAnswer.MayAbilityChosen(choice.move().destination() != Zone.HAND));
    }
}
