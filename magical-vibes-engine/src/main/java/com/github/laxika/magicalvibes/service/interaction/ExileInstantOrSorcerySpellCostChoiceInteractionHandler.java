package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Applies the spell choice for an activated ability's stack-spell exile cost. */
@Component
@RequiredArgsConstructor
public class ExileInstantOrSorcerySpellCostChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExileInstantOrSorcerySpellCostChoice> {

    private final AbilityActivationService abilityActivationService;

    @Override
    public Class<PendingInteraction.ExileInstantOrSorcerySpellCostChoice> handledType() {
        return PendingInteraction.ExileInstantOrSorcerySpellCostChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExileInstantOrSorcerySpellCostChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen == null || chosen.size() != 1 || !interaction.validCardIds().contains(chosen.getFirst())) {
            throw new IllegalStateException("Choose exactly one instant or sorcery spell you control");
        }

        abilityActivationService.handleActivatedAbilityExileInstantOrSorcerySpellCostChosen(
                gameData, player, interaction, chosen.getFirst());
    }
}
