package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaForEachOtherLandWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.AddProducedManaWhenLandOfSubtypeTappedEffect;
import com.github.laxika.magicalvibes.model.effect.AddProducedManaWhenSnowLandTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TappedSnowLandDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeWhenOpponentTapsLandOfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentTappedLandDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTappedLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TapLandsThatCouldProduceSameManaAsTappedLandEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
    private final GameLogService gameLogService;
    private final DamagePreventionService damagePreventionService;
    private final PermanentRemovalService permanentRemovalService;
    private final LifeSupport lifeSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentControlSupport permanentControlSupport;
    private final TriggerCollectionService triggerCollectionService;

    @CollectsTrigger(value = RemoveCounterFromSourceEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleControllerTapsLandRemoveCounter(TriggerMatchContext match,
            RemoveCounterFromSourceEffect effect, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.controllerId().equals(lt.tappingPlayerId())) {
            return false;
        }

        var gameData = match.gameData();
        var sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        return true;
    }

    @CollectsTrigger(value = DealDamageOnLandTapEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleDealDamageOnLandTap(TriggerMatchContext match,
            DealDamageOnLandTapEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        var gameData = match.gameData();
        var tappingPlayerId = lt.tappingPlayerId();
        if (trigger.landFilter() != null) {
            Permanent tappedLand = gameQueryService.findPermanentById(gameData, lt.tappedLandId());
            if (tappedLand == null
                    || !predicateEvaluationService.matchesPermanentPredicate(gameData, tappedLand, trigger.landFilter())) {
                return false;
            }
        }
        var sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        int damage = gameQueryService.applyDamageMultiplier(gameData, trigger.damage());
        damage += gameQueryService.getControllerDamageToOpponentBonus(
                gameData, match.controllerId(), tappingPlayerId);

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " triggers — deals " + damage + " damage to " + gameData.playerIdToName.get(tappingPlayerId) + "."));
        log.info("Game {} - {} triggers on land tap, dealing {} damage to {}",
                gameData.id, cardName, damage, gameData.playerIdToName.get(tappingPlayerId));

        CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, match.permanent());
        boolean sourceDamagePrevented = damagePreventionService.isSourceDamagePreventedForPlayer(
                gameData, tappingPlayerId, match.permanent().getId());
        if (sourceDamagePrevented && !gameQueryService.isDamageFromPermanentSourcePrevented(gameData, match.permanent())) {
            damagePreventionService.applySourceDamagePreventionForPlayer(
                    gameData, tappingPlayerId, match.permanent().getId(), damage,
                    gameQueryService.getEffectiveColors(gameData, match.permanent()));
        }
        if (!gameQueryService.isDamageFromPermanentSourcePrevented(gameData, match.permanent())
                && !sourceDamagePrevented
                && !gameData.isPreventedFromDealingDamage(match.permanent().getId())
                && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, tappingPlayerId, sourceColor)) {
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, tappingPlayerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, tappingPlayerId, effectiveDamage, cardName);
            effectiveDamage -= damagePreventionService.applyDamageToControllerAndPutCounterOnSelf(
                    gameData, tappingPlayerId, effectiveDamage);
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, tappingPlayerId)) {
                lifeSupport.applyPoisonCounters(gameData, tappingPlayerId, effectiveDamage,
                        cardName, match.controllerId());
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, tappingPlayerId)) {
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(tappingPlayerId) + "'s life total can't change."));
            } else {
                int lifeLoss = effectiveDamage
                        * gameQueryService.opponentLifeLossMultiplier(gameData, tappingPlayerId);
                gameData.playerLifeTotals.put(tappingPlayerId,
                        gameQueryService.lifeAfterDamage(gameData, tappingPlayerId, lifeLoss));
            }
            if (effectiveDamage > 0) {
                gameData.recordDamageToPlayer(tappingPlayerId, effectiveDamage,
                        gameQueryService.isArtifact(gameData, match.permanent()) ? effectiveDamage : 0);
                triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, tappingPlayerId, effectiveDamage);
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

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(match.controllerId())
                        + " gains " + trigger.lifeAmount() + " life."));
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
        var sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        ManaProducingEffect mana = trigger.mana();

        if (mana instanceof AwardAnyColorManaEffect anyColor) {
            int amount = amountEvaluationService.evaluate(gameData, anyColor.amount(),
                    new AmountContext(tappingPlayerId, null, null, 0, 0));
            if (!AnyColorManaChoiceSupport.beginColorChoice(interactionHandlerRegistry, gameData,
                    tappingPlayerId, anyColor, amount, false, null)) {
                return false;
            }

            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " triggers — " + playerName + " chooses a color of mana to add."));
            log.info("Game {} - Awaiting {} to choose a mana color from {}", gameData.id, playerName, cardName);
            return true;
        }

        if (mana instanceof AwardManaEffect award) {
            int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                    new AmountContext(tappingPlayerId, null, null, 0, 0));
            if (amount <= 0) {
                return false;
            }

            gameData.playerManaPools.get(tappingPlayerId).add(award.color(), amount);

            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " triggers - " + playerName + " adds " + amount + " " + award.color().name().toLowerCase() + " mana."));
            return true;
        }

        if (mana instanceof AwardManaOfColorsEffect ofColors) {
            int amount = amountEvaluationService.evaluate(gameData, ofColors.amount(),
                    new AmountContext(tappingPlayerId, null, null, 0, 0));
            if (amount <= 0 || ofColors.colors().isEmpty()) {
                return false;
            }

            if (ofColors.colors().size() == 1) {
                ManaColor manaColor = ofColors.colors().get(0);
                gameData.playerManaPools.get(tappingPlayerId).add(manaColor, amount);
                gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                        " triggers - " + playerName + " adds " + amount + " "
                                + manaColor.name().toLowerCase() + " mana."));
                return true;
            }

            ChoiceContext.ManaColorChoice choiceContext = ChoiceContext.ManaColorChoice
                    .fixedColorCombination(tappingPlayerId, false, amount, ofColors.colors());
            List<String> colors = ofColors.colors().stream().map(Enum::name).toList();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    tappingPlayerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " triggers — " + playerName + " chooses colors of mana to add."));
            log.info("Game {} - Awaiting {} to choose {} colors of mana from {}", gameData.id,
                    playerName, amount, cardName, colors);
            return true;
        }

        if (mana instanceof AwardChosenColorManaEffect) {
            CardColor chosenColor = match.permanent().getChosenColor();
            if (chosenColor == null) {
                return false;
            }

            ManaColor chosenManaColor = ManaColor.valueOf(chosenColor.name());
            gameData.playerManaPools.get(tappingPlayerId).add(chosenManaColor);
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " triggers - " + playerName + " adds 1 additional "
                            + chosenColor.name().toLowerCase() + " mana."));
            return true;
        }

        log.warn("Unsupported mana effect in AddManaOnEnchantedLandTapEffect: {}", mana.getClass().getSimpleName());
        return false;
    }

    @CollectsTrigger(value = AddExtraManaOfChosenColorOnLandTapEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddExtraManaOfChosenColor(TriggerMatchContext match,
            AddExtraManaOfChosenColorOnLandTapEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (trigger.controllerOnly() && !match.controllerId().equals(lt.tappingPlayerId())) return false;

        CardColor chosenColor = match.permanent().getChosenColor();
        if (chosenColor == null) return false;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (trigger.landFilter() != null
                && !predicateEvaluationService.matchesPermanentPredicate(
                        match.gameData(), tappedLand, trigger.landFilter())) return false;

        ManaColor chosenManaColor = ManaColor.valueOf(chosenColor.name());
        boolean producesChosenColor = tappedLand.getCard().getEffects(EffectSlot.ON_TAP).stream()
                .anyMatch(e -> e instanceof AwardManaEffect award && award.color() == chosenManaColor);
        if (!producesChosenColor) return false;

        ManaPool pool = match.gameData().playerManaPools.get(lt.tappingPlayerId());
        pool.add(chosenManaColor);

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                        + " adds 1 additional " + chosenColor.name().toLowerCase() + " mana."));
        return true;
    }

    @CollectsTrigger(value = AddOneOfEachManaTypeProducedByLandEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddOneOfEachManaType(TriggerMatchContext match,
            AddOneOfEachManaTypeProducedByLandEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        // Vorinclex fires only for the controller's own lands; Mana Flare is symmetric.
        if (trigger.controllerOnly() && !match.controllerId().equals(lt.tappingPlayerId())) return false;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (trigger.landFilter() != null
                && !predicateEvaluationService.matchesPermanentPredicate(
                        match.gameData(), tappedLand, trigger.landFilter())) return false;

        if (trigger.matchesImprintedCardName()) {
            Card imprintedCard = match.gameData().getImprintedCard(match.permanent().getCard());
            if (imprintedCard == null
                    || !imprintedCard.getName().equals(tappedLand.getCard().getName())) {
                return false;
            }
        }

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

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                        + " adds 1 additional " + producedColor.name().toLowerCase() + " mana."));
        return true;
    }

    @CollectsTrigger(value = AddManaForEachOtherLandWithSameNameEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaForEachOtherLandWithSameName(TriggerMatchContext match,
            AddManaForEachOtherLandWithSameNameEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;

        var gameData = match.gameData();
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, lt.tappedLandId());
        if (tappedLand == null || !gameQueryService.isLand(gameData, tappedLand)) return false;

        String tappedLandName = gameQueryService.getEffectiveName(gameData, tappedLand);
        long matchingLandCount = gameData.playerBattlefields
                .getOrDefault(lt.tappingPlayerId(), List.of()).stream()
                .filter(other -> !other.getId().equals(tappedLand.getId()))
                .filter(other -> gameQueryService.isLand(gameData, other))
                .filter(other -> tappedLandName.equals(gameQueryService.getEffectiveName(gameData, other)))
                .count();
        if (matchingLandCount == 0) return false;

        Set<ManaColor> producedColors = tappedLand.getCard().getEffects(EffectSlot.ON_TAP).stream()
                .filter(AwardManaEffect.class::isInstance)
                .map(AwardManaEffect.class::cast)
                .map(AwardManaEffect::color)
                .collect(Collectors.toSet());
        if (producedColors.isEmpty()) return false;

        ManaPool pool = gameData.playerManaPools.get(lt.tappingPlayerId());
        for (ManaColor color : producedColors) {
            pool.add(color, Math.toIntExact(matchingLandCount));
        }

        gameLogService.append(gameData, GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + gameData.playerIdToName.get(lt.tappingPlayerId())
                        + " adds " + matchingLandCount + " additional mana of each type produced by "
                        + tappedLand.getCard().getName() + "."));
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

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                        + " adds 1 additional " + producedColor.name().toLowerCase() + " mana."));
        return true;
    }

    @CollectsTrigger(value = AddProducedManaWhenSnowLandTappedEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddProducedManaWhenSnowLandTapped(TriggerMatchContext match,
            AddProducedManaWhenSnowLandTappedEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (!gameQueryService.hasEffectiveSupertype(match.gameData(), tappedLand, CardSupertype.SNOW)) return false;

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

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                        + " adds 1 additional " + producedColor.name().toLowerCase() + " mana."));
        return true;
    }

    @CollectsTrigger(value = TappedSnowLandDoesntUntapEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleTappedSnowLandDoesntUntap(TriggerMatchContext match,
            TappedSnowLandDoesntUntapEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (!gameQueryService.hasEffectiveSupertype(match.gameData(), tappedLand, CardSupertype.SNOW)) return false;

        tappedLand.setSkipUntapCount(tappedLand.getSkipUntapCount() + 1);

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                " triggers — ", tappedLand.getCard(), " doesn't untap during its controller's next untap step."));
        return true;
    }

    @CollectsTrigger(value = AddManaWhenLandOfSubtypeTappedForManaEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaWhenSubtypeLandTapped(TriggerMatchContext match,
            AddManaWhenLandOfSubtypeTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (!tappedLand.getCard().getSubtypes().contains(trigger.subtype())) return false;
        if (trigger.controllerOnly() && !match.controllerId().equals(lt.tappingPlayerId())) return false;

        // The tapping player is the land's controller and receives the additional mana.
        ManaPool pool = match.gameData().playerManaPools.get(lt.tappingPlayerId());
        pool.add(trigger.color());

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                        + " adds 1 additional " + trigger.color().name().toLowerCase() + " mana."));
        return true;
    }

    @CollectsTrigger(value = AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddRestrictedManaWhenSubtypeLandTapped(TriggerMatchContext match,
            AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (!tappedLand.getCard().getSubtypes().contains(trigger.subtype())) return false;

        boolean snow = gameQueryService.hasEffectiveSupertype(match.gameData(), tappedLand, CardSupertype.SNOW);
        int amount = snow ? trigger.snowAmount() : trigger.amount();
        ManaPool pool = match.gameData().playerManaPools.get(lt.tappingPlayerId());
        trigger.restriction().applyTo(pool, trigger.color(), amount);

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                        + " adds " + amount + " additional " + trigger.color().name().toLowerCase()
                        + " mana (" + trigger.restriction().description() + ")."));
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

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                " triggers — ", tappedLand.getCard(), " is returned to its owner's hand."));
        return true;
    }

    @CollectsTrigger(value = DestroyReferencedPermanentEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleLandTapDestroy(TriggerMatchContext match,
            DestroyReferencedPermanentEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (lt.tappingPlayerId().equals(match.gameData().activePlayerId)) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(lt.tappedLandId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to destroy a land tapped outside its controller's turn",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * "Whenever you tap this land for mana, target opponent creates ..." (Forbidden Orchard). Only the
     * source land's own taps by its own controller count, and the tokens go to that controller's
     * opponent — the sole legal target for the "target opponent" clause in a two-player game.
     */
    @CollectsTrigger(value = CreateTokenForTargetPlayerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleCreateTokenForOpponentOnSelfTapped(TriggerMatchContext match,
            CreateTokenForTargetPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.permanent().getId().equals(lt.tappedLandId())) return false;
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;

        UUID opponentId = gameQueryService.getOpponentId(match.gameData(), match.controllerId());
        if (opponentId == null) return false;

        permanentControlSupport.applyCreateToken(match.gameData(), opponentId, trigger.tokenEffect(),
                match.permanent().getCard().getSetCode());

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(opponentId)
                        + " creates a " + trigger.tokenEffect().tokenName() + " token."));
        return true;
    }

    @CollectsTrigger(value = RegisterDelayedChooseOpponentGainsControlOfSourceEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleDelayedControlChangeOnSelfTapped(TriggerMatchContext match,
            RegisterDelayedChooseOpponentGainsControlOfSourceEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.permanent().getId().equals(lt.tappedLandId())) return false;
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
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

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                " triggers — ", tappedLand.getCard(), " doesn't untap during its controller's next untap step."));
        return true;
    }

    /**
     * Queues Mana Web's triggered ability. The tapping player is stored as a non-targeting player
     * reference, while the tapped land is stored as the triggering permanent so both are available
     * when the ability resolves after players have had priority.
     */
    @CollectsTrigger(value = TapLandsThatCouldProduceSameManaAsTappedLandEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleManaWeb(TriggerMatchContext match,
            TapLandsThatCouldProduceSameManaAsTappedLandEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (match.controllerId().equals(lt.tappingPlayerId())) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                lt.tappingPlayerId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(lt.tappedLandId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on opponent land tap", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }
}
