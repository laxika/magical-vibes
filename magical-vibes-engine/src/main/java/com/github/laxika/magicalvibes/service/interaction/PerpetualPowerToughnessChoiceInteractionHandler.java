package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DamageBonusGrant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PerpetualPowerToughnessChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PerpetualPowerToughnessChoice> {

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PerpetualPowerToughnessChoice> handledType() {
        return PendingInteraction.PerpetualPowerToughnessChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PerpetualPowerToughnessChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        List<Card> hand = gameData.playerHands.getOrDefault(interaction.playerId(), List.of());
        if (!interaction.validIndices().contains(cardIndex)
                || cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalArgumentException("Choose a valid card from your hand");
        }

        Card chosenCard = hand.get(cardIndex);
        gameData.interaction.clearAwaitingInput();
        if (interaction.power() != 0 || interaction.toughness() != 0) {
            gameData.perpetualPowerToughnessModifiers.merge(
                    chosenCard.getId(),
                    new PerpetualPowerToughnessModifier(interaction.power(), interaction.toughness()),
                    PerpetualPowerToughnessModifier::add);
        }
        if (!interaction.grantedKeywords().isEmpty()) {
            gameData.perpetualKeywords.merge(chosenCard.getId(), interaction.grantedKeywords(),
                    (existing, added) -> {
                        Set<Keyword> merged = EnumSet.noneOf(Keyword.class);
                        merged.addAll(existing);
                        merged.addAll(added);
                        return Set.copyOf(merged);
                    });
        }
        if (interaction.genericCostReduction() != 0) {
            gameData.perpetualGenericCastCostIncreases.merge(
                    chosenCard.getId(), -interaction.genericCostReduction(), Integer::sum);
        }
        if (interaction.noncombatDamageBonus() != 0) {
            GameData.addDamageBonusGrant(gameData.perpetualNoncombatDamageBonuses, chosenCard.getId(),
                    new DamageBonusGrant(null, null, interaction.noncombatDamageBonus()));
        }
        if (interaction.spellCastingAbilityGrant() != null) {
            gameData.perpetualCardSpellCastingAbilityGrants.compute(chosenCard.getId(),
                    (ignored, existing) -> {
                        List<com.github.laxika.magicalvibes.model.effect.SpellCastingAbilityGrantingEffect> grants =
                                existing == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(existing);
                        if (!grants.contains(interaction.spellCastingAbilityGrant())) {
                            grants.add(interaction.spellCastingAbilityGrant());
                        }
                        return java.util.Collections.synchronizedList(grants);
                    });
        }
        if (interaction.perpetualManaCostIncrease() != null) {
            gameData.perpetualManaCostIncreases.merge(
                    chosenCard.getId(), new ManaCost(interaction.perpetualManaCostIncrease()),
                    ManaCost::increasedBy);
        }
        if (interaction.selfCastAbility() != null) {
            gameData.grantPerpetualAbility(chosenCard.getId(), EffectSlot.ON_SELF_CAST, interaction.selfCastAbility());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
