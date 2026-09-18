package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Shared "activate a mana ability of each land" path used by Drain Power and Pygmy Hippo.
 * Fixed single-color lands are exact; multi-ability lands use their first available tap-for-mana
 * ability, and any required color choice is made by the land's controller.
 */
@Component
@RequiredArgsConstructor
public class LandManaDrainSupport {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final TapUntapSupport tapUntapSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Activates a mana ability of each untapped land {@code playerId} controls, adding the produced
     * mana to that player's pool and tapping each land that produced.
     */
    public void activateManaAbilityOfEachLand(GameData gameData, UUID playerId) {
        activateManaAbilityOfEachLand(gameData, playerId, playerId);
    }

    public void activateManaAbilityOfEachLand(GameData gameData, UUID playerId, UUID manaRecipientId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        var battlefield = gameData.playerBattlefields.get(playerId);
        if (pool == null || battlefield == null) {
            return;
        }
        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (!perm.getCard().hasType(CardType.LAND) || perm.isTapped()) {
                continue;
            }
            if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                continue;
            }
            int multiplier = gameQueryService.manaProductionMultiplier(gameData, playerId, perm);
            if (produceLandMana(gameData, playerId, manaRecipientId, pool, perm, multiplier)) {
                tapUntapSupport.tapPermanent(gameData, perm);
            }
        }
    }

    /**
     * Adds the mana one untapped land would produce to {@code pool}. Returns true if a mana ability
     * was found (so the land should be tapped).
     */
    boolean produceLandMana(GameData gameData, UUID playerId, ManaPool pool, Permanent perm, int multiplier) {
        return produceLandMana(gameData, playerId, playerId, pool, perm, multiplier);
    }

    boolean produceLandMana(GameData gameData, UUID playerId, UUID manaRecipientId,
                            ManaPool pool, Permanent perm, int multiplier) {
        ManaColor fixedLandColor = gameQueryService.fixedLandManaColor(gameData, perm);
        if (fixedLandColor != null) {
            int amount = 0;
            ManaColor overridden = gameQueryService.getOverriddenLandManaColor(gameData, perm);
            if (overridden != null) {
                amount = multiplier;
            } else if (PotentialManaService.hasOnTapManaEffects(perm.getCard())) {
                for (CardEffect e : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (e instanceof AwardManaEffect award) {
                        amount += amountEvaluationService.evaluate(gameData, award.amount(),
                                AmountContext.forManaAbility(perm, playerId)) * multiplier;
                    } else if (e instanceof AwardAnyColorManaEffect anyColor && ordinaryPoolAnyColor(anyColor)) {
                        amount += evaluate(gameData, playerId, perm, anyColor) * multiplier;
                    }
                }
            } else {
                for (ActivatedAbility ability : perm.getCard().getActivatedAbilities()) {
                    if (!PotentialManaService.isFreeTapManaAbility(ability)) {
                        continue;
                    }
                    for (CardEffect e : ability.getEffects()) {
                        if (e instanceof AwardManaEffect award) {
                            amount += amountEvaluationService.evaluate(gameData, award.amount(),
                                    AmountContext.forManaAbility(perm, playerId)) * multiplier;
                        } else if (e instanceof AwardAnyColorManaEffect anyColor && ordinaryPoolAnyColor(anyColor)) {
                            amount += evaluate(gameData, playerId, perm, anyColor) * multiplier;
                        }
                    }
                    break;
                }
            }
            if (amount > 0) {
                pool.add(fixedLandColor, amount);
                return true;
            }
            return false;
        }
        Set<ManaColor> twisted = gameQueryService.twistedLandManaColors(gameData, perm);
        if (!twisted.isEmpty()) {
            ManaColor color = twisted.iterator().next();
            pool.add(color, multiplier);
            return true;
        }
        ManaColor overridden = gameQueryService.getOverriddenLandManaColor(gameData, perm);
        if (overridden != null) {
            pool.add(overridden, multiplier);
            return true;
        }
        if (PotentialManaService.hasOnTapManaEffects(perm.getCard())) {
            for (CardEffect e : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                if (e instanceof AwardManaEffect award) {
                    int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                            AmountContext.forManaAbility(perm, playerId)) * multiplier;
                    pool.add(award.color(), amount);
                } else if (e instanceof AwardAnyColorManaEffect anyColor && ordinaryPoolAnyColor(anyColor)) {
                    queueAnyColorChoice(gameData, playerId, manaRecipientId, anyColor,
                            evaluate(gameData, playerId, perm, anyColor) * multiplier,
                            availableColors(gameData, playerId, anyColor));
                }
            }
            return true;
        }
        for (ActivatedAbility ability : perm.getCard().getActivatedAbilities()) {
            if (!ability.isRequiresTap() || !AbilityActivationService.isManaAbility(ability)) {
                continue;
            }
            if (ability.getManaCost() != null) {
                ManaCost cost = new ManaCost(ability.getManaCost());
                if (!cost.canPay(pool)) {
                    continue;
                }
                cost.pay(pool);
            }
            for (CardEffect e : ability.getEffects()) {
                if (e instanceof AwardManaEffect award) {
                    int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                            AmountContext.forManaAbility(perm, playerId)) * multiplier;
                    pool.add(award.color(), amount);
                } else if (e instanceof AwardAnyColorManaEffect anyColor && ordinaryPoolAnyColor(anyColor)) {
                    queueAnyColorChoice(gameData, playerId, manaRecipientId, anyColor,
                            evaluate(gameData, playerId, perm, anyColor) * multiplier,
                            availableColors(gameData, playerId, anyColor));
                }
            }
            return true;
        }
        return false;
    }

    private void queueAnyColorChoice(GameData gameData, UUID playerId, UUID recipientPlayerId,
                                     AwardAnyColorManaEffect effect, int amount,
                                     java.util.List<ManaColor> availableColors) {
        if (amount <= 0 || availableColors.isEmpty()) {
            return;
        }
        ChoiceContext context = effect.grantsCommanderCounter()
                ? new ChoiceContext.CommanderCounterManaColorChoice(playerId, amount, recipientPlayerId)
                : new ChoiceContext.ChosenPlayerManaColorChoice(playerId, playerId, recipientPlayerId, false, amount);
        PendingInteraction.ColorChoice choice = new PendingInteraction.ColorChoice(
                playerId, null, null, context,
                availableColors.stream().map(Enum::name).toList(),
                "Choose a color of mana to add.");
        if (gameData.interaction.isAwaitingInput()) {
            gameData.queueInteraction(choice);
        } else {
            interactionHandlerRegistry.begin(gameData, choice);
        }
    }

    /**
     * Commander-identity mana is ordinary pool mana, but its available choices are restricted to
     * the activating player's commander color identity.
     */
    private static boolean ordinaryPoolAnyColor(AwardAnyColorManaEffect effect) {
        return effect.restriction() == ManaSpendRestriction.NONE
                || effect.restriction() == ManaSpendRestriction.COMMANDER_COLOR_IDENTITY
                || effect.restriction() == ManaSpendRestriction.COMMANDER_COLOR_IDENTITY_WITH_CREATURE_TYPE_SCRY;
    }

    private static java.util.List<ManaColor> availableColors(GameData gameData, UUID playerId,
                                                              AwardAnyColorManaEffect effect) {
        if (effect.restriction() == ManaSpendRestriction.COMMANDER_COLOR_IDENTITY
                || effect.restriction() == ManaSpendRestriction.COMMANDER_COLOR_IDENTITY_WITH_CREATURE_TYPE_SCRY) {
            return ManaProductionSupport.commanderColorIdentity(gameData, playerId);
        }
        return ManaColor.COLORS;
    }

    private int evaluate(GameData gameData, UUID playerId, Permanent perm, AwardAnyColorManaEffect effect) {
        return amountEvaluationService.evaluate(gameData, effect.amount(),
                AmountContext.forManaAbility(perm, playerId));
    }
}
