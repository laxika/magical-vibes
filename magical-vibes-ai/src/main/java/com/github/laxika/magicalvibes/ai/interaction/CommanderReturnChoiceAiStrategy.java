package com.github.laxika.magicalvibes.ai.interaction;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
class CommanderReturnChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.CommanderReturnChoice> {
    public Class<PendingInteraction.CommanderReturnChoice> handledType() { return PendingInteraction.CommanderReturnChoice.class; }
    public void answer(PendingInteraction.CommanderReturnChoice choice, AiInteractionContext ctx) throws Exception {
        ctx.gameActions().answerInteraction(new InteractionAnswer.MayAbilityChosen(choice.fromZone() != Zone.HAND));
    }
}
