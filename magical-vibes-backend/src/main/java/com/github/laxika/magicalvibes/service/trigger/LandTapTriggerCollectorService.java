package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentTappedLandDoesntUntapEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Trigger collectors for land-tap events (ON_ANY_PLAYER_TAPS_LAND).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LandTapTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final DamagePreventionService damagePreventionService;
    private final PermanentRemovalService permanentRemovalService;

    @CollectsTrigger(value = DealDamageOnLandTapEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleDealDamageOnLandTap(TriggerMatchContext match,
            DealDamageOnLandTapEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        var gameData = match.gameData();
        var tappingPlayerId = lt.tappingPlayerId();
        String cardName = match.permanent().getCard().getName();
        int damage = gameQueryService.applyDamageMultiplier(gameData, trigger.damage());

        String logEntry = cardName + " triggers — deals " + damage + " damage to "
                + gameData.playerIdToName.get(tappingPlayerId) + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} triggers on land tap, dealing {} damage to {}",
                gameData.id, cardName, damage, gameData.playerIdToName.get(tappingPlayerId));

        if (!gameQueryService.isDamageFromSourcePrevented(gameData, match.permanent().getEffectiveColor())
                && !damagePreventionService.isSourceDamagePreventedForPlayer(gameData, tappingPlayerId, match.permanent().getId())
                && !gameData.permanentsPreventedFromDealingDamage.contains(match.permanent().getId())
                && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, tappingPlayerId, match.permanent().getEffectiveColor())) {
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, tappingPlayerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, tappingPlayerId, effectiveDamage, cardName);
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, tappingPlayerId)) {
                if (gameQueryService.canPlayerGetPoisonCounters(gameData, tappingPlayerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(tappingPlayerId, 0);
                    gameData.playerPoisonCounters.put(tappingPlayerId, currentPoison + effectiveDamage);
                    gameBroadcastService.logAndBroadcast(gameData,
                            gameData.playerIdToName.get(tappingPlayerId) + " gets " + effectiveDamage + " poison counters from " + cardName + ".");
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, tappingPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        gameData.playerIdToName.get(tappingPlayerId) + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(tappingPlayerId);
                gameData.playerLifeTotals.put(tappingPlayerId, currentLife - effectiveDamage);
            }
            if (effectiveDamage > 0) {
                gameData.playersDealtDamageThisTurn.add(tappingPlayerId);
            }
        }

        return true;
    }

    @CollectsTrigger(value = AddManaOnEnchantedLandTapEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaOnEnchantedLandTap(TriggerMatchContext match,
            AddManaOnEnchantedLandTapEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.permanent().isAttached()
                || !match.permanent().getAttachedTo().equals(lt.tappedLandId())) {
            return false;
        }

        ManaPool pool = match.gameData().playerManaPools.get(lt.tappingPlayerId());
        for (int i = 0; i < trigger.amount(); i++) {
            pool.add(trigger.color());
        }

        String logEntry = match.permanent().getCard().getName() + " triggers - "
                + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                + " adds " + trigger.amount() + " " + trigger.color().name().toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(match.gameData(), logEntry);
        return true;
    }

    @CollectsTrigger(value = AddExtraManaOfChosenColorOnLandTapEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddExtraManaOfChosenColor(TriggerMatchContext match,
            AddExtraManaOfChosenColorOnLandTapEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        // Only triggers for the controller's own lands
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;

        CardColor chosenColor = match.permanent().getChosenColor();
        if (chosenColor == null) return false;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;

        ManaColor chosenManaColor = ManaColor.valueOf(chosenColor.name());
        boolean producesChosenColor = tappedLand.getCard().getEffects(EffectSlot.ON_TAP).stream()
                .anyMatch(e -> e instanceof AwardManaEffect award && award.color() == chosenManaColor);
        if (!producesChosenColor) return false;

        ManaPool pool = match.gameData().playerManaPools.get(lt.tappingPlayerId());
        pool.add(chosenManaColor);

        String logEntry = match.permanent().getCard().getName() + " triggers — "
                + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                + " adds 1 additional " + chosenColor.name().toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(match.gameData(), logEntry);
        return true;
    }

    @CollectsTrigger(value = AddOneOfEachManaTypeProducedByLandEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddOneOfEachManaType(TriggerMatchContext match,
            AddOneOfEachManaTypeProducedByLandEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        // Only triggers for the controller's own lands
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;

        ManaColor producedColor = null;
        for (CardEffect tapEffect : tappedLand.getCard().getEffects(EffectSlot.ON_TAP)) {
            if (tapEffect instanceof AwardManaEffect awardMana) {
                producedColor = awardMana.color();
                break;
            }
        }
        if (producedColor == null) return false;

        ManaPool pool = match.gameData().playerManaPools.get(lt.tappingPlayerId());
        pool.add(producedColor);

        String logEntry = match.permanent().getCard().getName() + " triggers — "
                + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                + " adds 1 additional " + producedColor.name().toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(match.gameData(), logEntry);
        return true;
    }

    @CollectsTrigger(value = OpponentTappedLandDoesntUntapEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleOpponentLandDoesntUntap(TriggerMatchContext match,
            OpponentTappedLandDoesntUntapEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        // Only triggers for opponents' lands
        if (match.controllerId().equals(lt.tappingPlayerId())) return false;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;

        tappedLand.setSkipUntapCount(tappedLand.getSkipUntapCount() + 1);

        String logEntry = match.permanent().getCard().getName() + " triggers — "
                + tappedLand.getCard().getName()
                + " doesn't untap during its controller's next untap step.";
        gameBroadcastService.logAndBroadcast(match.gameData(), logEntry);
        return true;
    }
}
