package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrainTargetPlayersLandManaEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link DrainTargetPlayersLandManaEffect} (Drain Power): each untapped land the target
 * player controls is tapped for the mana it produces (added to that player's pool), then the
 * target player's entire pool is emptied and the spell's controller adds an equal amount of mana.
 *
 * <p>Lands whose mana ability requires a color choice (any-color producers) contribute colorless,
 * and dual/multi-ability lands use their first tap-for-mana ability; the common case of fixed
 * single-color lands is exact.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrainTargetPlayersLandManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final TapUntapSupport tapUntapSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrainTargetPlayersLandManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        ManaPool targetPool = gameData.playerManaPools.get(targetPlayerId);
        ManaPool controllerPool = gameData.playerManaPools.get(entry.getControllerId());
        if (targetPool == null || controllerPool == null) {
            return;
        }

        int multiplier = gameQueryService.manaProductionMultiplier(gameData, targetPlayerId);

        // Activate a mana ability of each land the target player controls; the mana goes into
        // their own pool first (CR: they then lose all unspent mana).
        var battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent perm : new ArrayList<>(battlefield)) {
                if (!perm.getCard().hasType(CardType.LAND) || perm.isTapped()) {
                    continue;
                }
                if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                    continue;
                }
                if (produceLandMana(gameData, targetPlayerId, targetPool, perm, multiplier)) {
                    tapUntapSupport.tapPermanent(gameData, perm);
                }
            }
        }

        // The target player loses all unspent mana; the controller adds the mana lost this way.
        Map<String, Integer> lostByCode = targetPool.toMap();
        int totalTransferred = 0;
        for (ManaColor color : ManaColor.values()) {
            int amount = lostByCode.getOrDefault(color.getCode(), 0);
            if (amount > 0) {
                controllerPool.add(color, amount);
                totalTransferred += amount;
            }
        }
        targetPool.clear();
        targetPool.clearPersistentMana();

        String logMsg = entry.getCard().getName() + " drains " + totalTransferred + " mana.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" drains " + totalTransferred + " mana.").build());
        log.info("Game {} - {} drains {} mana from target player", gameData.id, entry.getCard().getName(), totalTransferred);
    }

    /**
     * Adds the mana one untapped land would produce to {@code pool}. Returns true if a mana ability
     * was found (so the land should be tapped). A land override (e.g. Evil Presence) and fixed
     * {@code AwardManaEffect} outputs are exact; any-color producers contribute colorless.
     */
    private boolean produceLandMana(GameData gameData, UUID playerId, ManaPool pool, Permanent perm, int multiplier) {
        ManaColor fixedLandColor = gameQueryService.fixedLandManaColor(gameData);
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
                    } else if (e instanceof AwardAnyColorManaEffect aace) {
                        amount += aace.amount() * multiplier;
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
                        } else if (e instanceof AwardAnyColorManaEffect aace) {
                            amount += aace.amount() * multiplier;
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
            // Multi-type: pick one deterministically for this non-interactive drain path.
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
                } else if (e instanceof AwardAnyColorManaEffect aace) {
                    pool.add(ManaColor.COLORLESS, aace.amount() * multiplier);
                }
            }
            return true;
        }
        // Dual/utility lands: activate the first free tap-for-mana ability.
        for (ActivatedAbility ability : perm.getCard().getActivatedAbilities()) {
            if (!PotentialManaService.isFreeTapManaAbility(ability)) {
                continue;
            }
            for (CardEffect e : ability.getEffects()) {
                if (e instanceof AwardManaEffect award) {
                    int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                            AmountContext.forManaAbility(perm, playerId)) * multiplier;
                    pool.add(award.color(), amount);
                } else if (e instanceof AwardAnyColorManaEffect aace) {
                    pool.add(ManaColor.COLORLESS, aace.amount() * multiplier);
                }
            }
            return true;
        }
        return false;
    }
}
