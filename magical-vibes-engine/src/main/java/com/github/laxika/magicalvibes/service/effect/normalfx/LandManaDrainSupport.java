package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Shared "activate a mana ability of each land" path used by Drain Power and Pygmy Hippo.
 * Fixed single-color lands are exact; multi-ability lands use their first tap-for-mana ability;
 * any-color producers contribute colorless.
 */
@Component
@RequiredArgsConstructor
public class LandManaDrainSupport {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final TapUntapSupport tapUntapSupport;

    /**
     * Activates a mana ability of each untapped land {@code playerId} controls, adding the produced
     * mana to that player's pool and tapping each land that produced.
     */
    public void activateManaAbilityOfEachLand(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        var battlefield = gameData.playerBattlefields.get(playerId);
        if (pool == null || battlefield == null) {
            return;
        }
        int multiplier = gameQueryService.manaProductionMultiplier(gameData, playerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (!perm.getCard().hasType(CardType.LAND) || perm.isTapped()) {
                continue;
            }
            if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                continue;
            }
            if (produceLandMana(gameData, playerId, pool, perm, multiplier)) {
                tapUntapSupport.tapPermanent(gameData, perm);
            }
        }
    }

    /**
     * Adds the mana one untapped land would produce to {@code pool}. Returns true if a mana ability
     * was found (so the land should be tapped).
     */
    boolean produceLandMana(GameData gameData, UUID playerId, ManaPool pool, Permanent perm, int multiplier) {
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
                    } else if (e instanceof AwardAnyColorManaEffect anyColor && unrestricted(anyColor)) {
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
                        } else if (e instanceof AwardAnyColorManaEffect anyColor && unrestricted(anyColor)) {
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
                } else if (e instanceof AwardAnyColorManaEffect anyColor && unrestricted(anyColor)) {
                    pool.add(ManaColor.COLORLESS, evaluate(gameData, playerId, perm, anyColor) * multiplier);
                }
            }
            return true;
        }
        for (ActivatedAbility ability : perm.getCard().getActivatedAbilities()) {
            if (!PotentialManaService.isFreeTapManaAbility(ability)) {
                continue;
            }
            for (CardEffect e : ability.getEffects()) {
                if (e instanceof AwardManaEffect award) {
                    int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                            AmountContext.forManaAbility(perm, playerId)) * multiplier;
                    pool.add(award.color(), amount);
                } else if (e instanceof AwardAnyColorManaEffect anyColor && unrestricted(anyColor)) {
                    pool.add(ManaColor.COLORLESS, evaluate(gameData, playerId, perm, anyColor) * multiplier);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Spend-restricted any-color mana is skipped: this path pays plain colorless into the ordinary
     * pool, which would launder away the restriction the printed ability puts on it (CR 106.6).
     */
    private static boolean unrestricted(AwardAnyColorManaEffect effect) {
        return effect.restriction() == ManaSpendRestriction.NONE;
    }

    private int evaluate(GameData gameData, UUID playerId, Permanent perm, AwardAnyColorManaEffect effect) {
        return amountEvaluationService.evaluate(gameData, effect.amount(),
                AmountContext.forManaAbility(perm, playerId));
    }
}
