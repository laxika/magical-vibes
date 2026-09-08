package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Chooses the highest-mana-value eligible instant or sorcery to copy from the target hand. */
@Slf4j
class TargetHandSpellCopyChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.TargetHandSpellCopyChoice> {

    @Override
    public Class<PendingInteraction.TargetHandSpellCopyChoice> handledType() {
        return PendingInteraction.TargetHandSpellCopyChoice.class;
    }

    @Override
    public void answer(PendingInteraction.TargetHandSpellCopyChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        Set<UUID> validIds = Set.copyOf(interaction.validCardIds());
        List<UUID> chosen = interaction.cards().stream()
                .filter(card -> validIds.contains(card.getId()))
                .max(Comparator.comparingInt(Card::getManaValue))
                .map(card -> List.of(card.getId()))
                .orElseGet(List::of);

        log.info("AI: Choosing a spell to copy from the target hand in game {}", ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
