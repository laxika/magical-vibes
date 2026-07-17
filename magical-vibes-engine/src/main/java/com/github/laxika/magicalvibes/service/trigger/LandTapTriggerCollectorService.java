package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.AddProducedManaWhenLandOfSubtypeTappedEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeWhenOpponentTapsLandOfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentTappedLandDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTappedLandToHandEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.github.laxika.magicalvibes.model.GameLog;
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
    private final LifeSupport lifeSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;

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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} triggers on land tap, dealing {} damage to {}",
                gameData.id, cardName, damage, gameData.playerIdToName.get(tappingPlayerId));

        CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, match.permanent());
        if (!gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                && !damagePreventionService.isSourceDamagePreventedForPlayer(gameData, tappingPlayerId, match.permanent().getId())
                && !gameData.permanentsPreventedFromDealingDamage.contains(match.permanent().getId())
                && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, tappingPlayerId, sourceColor)) {
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, tappingPlayerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, tappingPlayerId, effectiveDamage, cardName);
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, tappingPlayerId)) {
                if (gameQueryService.canPlayerGetPoisonCounters(gameData, tappingPlayerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(tappingPlayerId, 0);
                    gameData.playerPoisonCounters.put(tappingPlayerId, currentPoison + effectiveDamage);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(tappingPlayerId) + " gets " + effectiveDamage + " poison counters from " + cardName + "."));
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, tappingPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(tappingPlayerId) + "'s life total can't change."));
            } else {
                int currentLife = gameData.getLife(tappingPlayerId);
                gameData.playerLifeTotals.put(tappingPlayerId, currentLife - effectiveDamage);
            }
            if (effectiveDamage > 0) {
                gameData.recordDamageToPlayer(tappingPlayerId, effectiveDamage);
            }
        }

        return true;
    }

    @CollectsTrigger(value = GainLifeWhenOpponentTapsLandOfSubtypeEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleGainLifeWhenOpponentTapsSubtypeLand(TriggerMatchContext match,
            GainLifeWhenOpponentTapsLandOfSubtypeEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        // Only opponents' land taps
        if (match.controllerId().equals(lt.tappingPlayerId())) return false;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (!tappedLand.getCard().getSubtypes().contains(trigger.subtype())) return false;

        lifeSupport.applyGainLife(match.gameData(), match.controllerId(), trigger.lifeAmount());

        String logEntry = match.permanent().getCard().getName() + " triggers — "
                + match.gameData().playerIdToName.get(match.controllerId())
                + " gains " + trigger.lifeAmount() + " life.";
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(logEntry));
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

        var gameData = match.gameData();
        UUID tappingPlayerId = lt.tappingPlayerId();
        String playerName = gameData.playerIdToName.get(tappingPlayerId);
        String cardName = match.permanent().getCard().getName();
        ManaProducingEffect mana = trigger.mana();

        if (mana instanceof AwardAnyColorManaEffect anyColor) {
            ChoiceContext.ManaColorChoice choiceContext =
                    new ChoiceContext.ManaColorChoice(tappingPlayerId, false, anyColor.amount());
            List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    tappingPlayerId, null, null, choiceContext, colors, "Choose a color of mana to add."));

            String logEntry = cardName + " triggers — " + playerName + " chooses a color of mana to add.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - Awaiting {} to choose a mana color from {}", gameData.id, playerName, cardName);
            return true;
        }

        if (mana instanceof AwardManaEffect award) {
            int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                    new AmountContext(tappingPlayerId, null, null, 0, 0, false));
            if (amount <= 0) {
                return false;
            }

            gameData.playerManaPools.get(tappingPlayerId).add(award.color(), amount);

            String logEntry = cardName + " triggers - " + playerName
                    + " adds " + amount + " " + award.color().name().toLowerCase() + " mana.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return true;
        }

        log.warn("Unsupported mana effect in AddManaOnEnchantedLandTapEffect: {}", mana.getClass().getSimpleName());
        return false;
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
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(logEntry));
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
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(logEntry));
        return true;
    }

    @CollectsTrigger(value = AddProducedManaWhenLandOfSubtypeTappedEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddProducedManaWhenSubtypeLandTapped(TriggerMatchContext match,
            AddProducedManaWhenLandOfSubtypeTappedEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        boolean subtypeMatches = trigger.subtypes().stream()
                .anyMatch(tappedLand.getCard().getSubtypes()::contains);
        if (!subtypeMatches) return false;

        ManaColor producedColor = null;
        for (CardEffect tapEffect : tappedLand.getCard().getEffects(EffectSlot.ON_TAP)) {
            if (tapEffect instanceof AwardManaEffect awardMana) {
                producedColor = awardMana.color();
                break;
            }
        }
        if (producedColor == null) return false;

        // "That player adds..." — the tapping player is the land's controller.
        ManaPool pool = match.gameData().playerManaPools.get(lt.tappingPlayerId());
        pool.add(producedColor);

        String logEntry = match.permanent().getCard().getName() + " triggers — "
                + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                + " adds 1 additional " + producedColor.name().toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(logEntry));
        return true;
    }

    @CollectsTrigger(value = AddManaWhenLandOfSubtypeTappedForManaEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaWhenSubtypeLandTapped(TriggerMatchContext match,
            AddManaWhenLandOfSubtypeTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (!tappedLand.getCard().getSubtypes().contains(trigger.subtype())) return false;

        // "Its controller adds an additional {G}" — the tapping player is the land's controller.
        ManaPool pool = match.gameData().playerManaPools.get(lt.tappingPlayerId());
        pool.add(trigger.color());

        String logEntry = match.permanent().getCard().getName() + " triggers — "
                + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                + " adds 1 additional " + trigger.color().name().toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(logEntry));
        return true;
    }

    @CollectsTrigger(value = ReturnTappedLandToHandEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleReturnTappedLandToHand(TriggerMatchContext match,
            ReturnTappedLandToHandEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        // Null when another Storm Cauldron's trigger already returned this land to hand.
        if (tappedLand == null) return false;
        if (!permanentRemovalService.removePermanentToHand(match.gameData(), tappedLand)) return false;

        String logEntry = match.permanent().getCard().getName() + " triggers — "
                + tappedLand.getCard().getName() + " is returned to its owner's hand.";
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(logEntry));
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
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(logEntry));
        return true;
    }
}
