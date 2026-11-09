package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfColorTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaForEachOtherLandWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.action.DelayedChooseOpponentGainsControlOfSource;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTappedLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapLandsThatCouldProduceSameManaAsTappedLandEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.TappedLandManaSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;

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
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final ObjectProvider<TriggerCollectionService> triggerCollectionServiceProvider;

    @CollectsTapTrigger(value = SequenceEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleManaAbilitySequence(TriggerMatchContext match,
            SequenceEffect sequence, TriggerContext ctx) {
        if (sequence.steps().size() == 2
                && sequence.steps().get(0) instanceof AddManaOnEnchantedLandTapEffect mana
                && sequence.steps().get(1) instanceof IntensifySourceCardEffect intensify) {
            if (!handleAddManaOnEnchantedLandTap(match, mana, ctx)) {
                return false;
            }
            match.gameData().intensifyCard(match.permanent().getCard(), intensify.amount());
            return true;
        }
        if (sequence.steps().size() == 2
                && sequence.steps().get(0) instanceof AddManaOnEnchantedLandTapEffect mana
                && sequence.steps().get(1) instanceof PutCountersOnSelfEffect intensify) {
            if (!handleAddManaOnEnchantedLandTap(match, mana, ctx)) {
                return false;
            }

            var source = match.permanent();
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    match.controllerId(),
                    source.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(intensify)),
                    null,
                    source.getId());
            entry.setNonTargeting(true);
            entry.setSourcePermanentSnapshot(new Permanent(source));
            match.gameData().enqueueTrigger(entry);
            return true;
        }

        if (!(sequence.steps().getFirst() instanceof AddOneOfEachManaTypeProducedByLandEffect mana)
                || !manaAbilityTriggers(match, mana, (TriggerContext.LandTap) ctx)) {
            return false;
        }
        // "That player adds one mana of any type that land produced, and this / that land deals 1 damage to them"
        // (Overabundance, Barbflare Gremlin), "… That land doesn't untap during its controller's next untap step"
        // (Winter's Night): a triggered mana ability (CR 605.1b), which resolves at once (CR 605.4a), each step by its
        // own handler.
        TriggerContext.LandTap landTap = (TriggerContext.LandTap) ctx;
        StackEntry entry = landTapEntry(match, sequence.steps(), landTap);
        resolveOrAwaitProducedMana(match.gameData(), entry, landTap, !mana.anyColor());
        return true;
    }

    /**
     * Collects a land-tap ability whose effect no handler of its own reads: the ability goes on the stack as the card
     * wrote it (it can't add mana, so it isn't a mana ability, CR 605.1b, and uses the stack), with the player who
     * tapped the land as its non-targeting "that player" and triggering player, and the land as its triggering
     * permanent ("that land"), both read as it resolves. The trigger's filters were judged by the caller: "whenever a
     * player taps a land for mana, this deals 1 damage to that player" (Manabarbs, Tember City), "… an Island …"
     * (Scald), "whenever an opponent taps a Mountain for mana, you may gain 1 life" (Sanctimony), "that land doesn't
     * untap" (Vorinclex, Voice of Hunger).
     */
    @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleLandTapAbility(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.LandTap landTap = (TriggerContext.LandTap) ctx;
        StackEntry entry = landTapEntry(match, effect, landTap);
        var gameData = match.gameData();
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(entry.getCard()));
        log.info("Game {} - {} triggers on land tap by {}", gameData.id,
                entry.getCard().getName(), gameData.playerIdToName.get(landTap.tappingPlayerId()));
        return true;
    }

    /**
     * The entry of the land-tap ability of {@code match}'s source with {@code effect}: the tapping player as its
     * non-targeting "that player" and triggering player, the tapped land as its triggering permanent, and the source
     * (a permanent, or a face-up plane) as it was when the ability triggered.
     */
    private StackEntry landTapEntry(TriggerMatchContext match, CardEffect effect, TriggerContext.LandTap landTap) {
        return landTapEntry(match, List.of(effect), landTap);
    }

    /**
     * The entry of the land-tap ability of {@code match}'s source with {@code effects}, as
     * {@link #landTapEntry(TriggerMatchContext, CardEffect, TriggerContext.LandTap)}; a player's granted ability
     * (Selesnya Loft Gardens' chaos) has no source card.
     */
    private StackEntry landTapEntry(TriggerMatchContext match, List<CardEffect> effects,
                                    TriggerContext.LandTap landTap) {
        Card sourceCard = match.sourceCard() != null ? match.sourceCard()
                : match.permanent() != null ? match.permanent().getCard() : null;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard == null ? "Mana ability" : sourceCard.getName() + "'s ability",
                new ArrayList<>(effects),
                landTap.tappingPlayerId(),
                match.permanent() == null ? null : match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPlayerId(landTap.tappingPlayerId());
        entry.setTriggeringPermanentId(landTap.tappedLandId());
        if (match.permanent() != null) {
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        }
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        }
        return entry;
    }

    /**
     * Resolves the land-tap triggered mana ability {@code entry} at once (CR 605.4a), reading the mana the tap produced
     * from the entry and the tapped land, as it is or as it last existed (CR 608.2h), from its triggering permanent; or,
     * when its effects add mana of the type the land produced ({@code readsProducedMana}) and the land's own color
     * choice is still open, parks it with its own effects until the choice settles the produced color
     * ({@code ChoiceHandlerService.resolveProducedManaTriggers} resolves it then).
     */
    private void resolveOrAwaitProducedMana(GameData gameData, StackEntry entry, TriggerContext.LandTap landTap,
                                            boolean readsProducedMana) {
        entry.setTriggeringPermanent(tappedLandAsItLastExisted(gameData, landTap.tappedLandId()));
        if (readsProducedMana && landTap.producedColors().isEmpty()
                && gameData.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            entry.setAwaitingProducedManaColor(true);
            gameData.pendingManaAbilityTriggers.add(entry);
            return;
        }
        entry.setProducedManaColors(landTap.producedColors());
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect effect = entry.getEffectsToResolve().get(i);
            EffectHandler handler = effectHandlerRegistry.getHandler(effect);
            if (handler == null) {
                log.warn("No handler for triggered mana ability effect: {}", effect.getClass().getSimpleName());
                continue;
            }
            entry.setResolvingEffectIndex(i);
            handler.resolve(gameData, entry, effect);
        }
        entry.setResolvingEffectIndex(-1);
    }

    /**
     * The tapped land as it is on the battlefield, or as it last existed there once its mana ability made it leave
     * (Gemstone Mine's last use), for "that land deals 1 damage" (CR 608.2h); {@code null} when neither is known.
     */
    private Permanent tappedLandAsItLastExisted(GameData gameData, UUID tappedLandId) {
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, tappedLandId);
        if (tappedLand != null) {
            return tappedLand;
        }
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> snapshot.getId().equals(tappedLandId))
                .findFirst().orElse(null);
    }

    @CollectsTapTrigger(value = RemoveCounterFromSourceEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
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

    @CollectsTapTrigger(value = AddManaOnEnchantedLandTapEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaOnEnchantedLandTap(TriggerMatchContext match,
            AddManaOnEnchantedLandTapEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.permanent().isAttached()
                || !match.permanent().getAttachedTo().equals(lt.tappedLandId())) {
            return false;
        }

        var gameData = match.gameData();
        UUID tappingPlayerId = gameQueryService.findPermanentController(gameData, lt.tappedLandId());
        if (tappingPlayerId == null) {
            return false;
        }
        String playerName = gameData.playerIdToName.get(tappingPlayerId);
        var sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        ManaProducingEffect mana = trigger.mana();

        if (mana instanceof AwardAnyColorManaEffect anyColor) {
            int amount = amountEvaluationService.evaluate(gameData, anyColor.amount(),
                    new AmountContext(tappingPlayerId, match.permanent(), null, 0, 0));
            boolean snowSource = gameQueryService.hasEffectiveSupertype(gameData, match.permanent(), CardSupertype.SNOW);
            if (!AnyColorManaChoiceSupport.beginColorChoice(interactionHandlerRegistry, gameData,
                    tappingPlayerId, anyColor, amount, false, null, null, null, null, snowSource)) {
                return false;
            }

            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " triggers — " + playerName + " chooses a color of mana to add."));
            log.info("Game {} - Awaiting {} to choose a mana color from {}", gameData.id, playerName, cardName);
            return true;
        }

        if (mana instanceof AwardManaEffect award) {
            int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                    new AmountContext(tappingPlayerId, match.permanent(), null, 0, 0));
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
                    new AmountContext(tappingPlayerId, match.permanent(), null, 0, 0));
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
            AnyColorManaChoiceSupport.beginOrQueueChoice(interactionHandlerRegistry, gameData, new PendingInteraction.ColorChoice(
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

    @CollectsTapTrigger(value = PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleCampLandTap(TriggerMatchContext match,
            PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect trigger,
            TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        Permanent source = match.permanent();
        if (source == null || !source.isAttached()
                || !lt.tappedLandId().equals(source.getAttachedTo())) {
            return false;
        }

        if (lt.producedColors().isEmpty()
                && match.gameData().interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    match.controllerId(),
                    source.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(trigger)),
                    null,
                    source.getId());
            entry.setSourcePermanentSnapshot(new Permanent(source));
            entry.setTargetFilter(TargetFilters.creatureYouControl());
            entry.setAwaitingProducedManaColor(true);
            match.gameData().pendingManaAbilityTriggers.add(entry);
            return true;
        }

        if (lt.producedColors().isEmpty()) {
            return false;
        }

        queueCampTargetChoice(match.gameData(), source.getCard(), match.controllerId(),
                source.getId(), new Permanent(source), trigger, lt.producedColors());
        return true;
    }

    /**
     * Completes an ability parked while an any-color mana choice was pending whose target is still to be chosen
     * (C.A.M.P., parked with its target filter): its target is chosen as it goes on the stack, with {@code manaColor}
     * as the mana the tap produced.
     */
    public boolean queueResolvedProducedManaTriggerTarget(GameData gameData, StackEntry entry,
            ManaColor manaColor) {
        if (entry.getTargetFilter() == null) {
            return false;
        }
        queueCampTargetChoice(gameData, entry.getCard(), entry.getControllerId(),
                entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot(),
                List.copyOf(entry.getEffectsToResolve()), Set.of(manaColor));
        return true;
    }

    private void queueCampTargetChoice(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, Permanent sourcePermanentSnapshot, CardEffect effect,
            Set<ManaColor> producedManaColors) {
        queueCampTargetChoice(gameData, sourceCard, controllerId, sourcePermanentId, sourcePermanentSnapshot,
                List.of(effect), producedManaColors);
    }

    private void queueCampTargetChoice(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, Permanent sourcePermanentSnapshot, List<CardEffect> effects,
            Set<ManaColor> producedManaColors) {
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                sourceCard,
                controllerId,
                effects,
                false,
                TargetFilters.creatureYouControl(),
                0,
                sourcePermanentId,
                sourcePermanentSnapshot).withProducedManaColors(producedManaColors));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        if (!gameData.interaction.isAwaitingInput()) {
            triggerCollectionServiceProvider.getObject().processNextSpellTargetTrigger(gameData);
        }
    }

    @CollectsTapTrigger(value = AddExtraManaOfChosenColorOnLandTapEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
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
        // A tap reports the colors it actually added; one that reports none falls back to the land's
        // basic land types.
        boolean producesChosenColor = lt.producedColors().isEmpty()
                ? TappedLandManaSupport.intrinsicManaColors(tappedLand).contains(chosenManaColor)
                : lt.producedColors().contains(chosenManaColor);
        if (!producesChosenColor) return false;

        UUID landControllerId = gameQueryService.findPermanentController(
                match.gameData(), tappedLand.getId());
        if (landControllerId == null) return false;
        ManaPool pool = match.gameData().playerManaPools.get(landControllerId);
        pool.add(chosenManaColor);

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(landControllerId)
                        + " adds 1 additional " + chosenColor.name().toLowerCase() + " mana."));
        return true;
    }

    /**
     * "Whenever [a player / you] tap[s] a land for mana, that player adds one mana of any type that land produced"
     * (Mana Flare, Vorinclex, Voice of Hunger, Extraplanar Lens, Regal Behemoth's "one mana of any color", a player's
     * granted chaos ability): a triggered mana ability resolved at once by its effect's handler.
     */
    @CollectsTapTrigger(value = AddOneOfEachManaTypeProducedByLandEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddOneOfEachManaType(TriggerMatchContext match,
            AddOneOfEachManaTypeProducedByLandEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!manaAbilityTriggers(match, trigger, lt)) return false;
        resolveOrAwaitProducedMana(match.gameData(), landTapEntry(match, trigger, lt), lt, !trigger.anyColor());
        return true;
    }

    /**
     * The trigger conditions of {@code trigger}, judged as the land is tapped: "you tap" (the tapping player is the
     * ability's controller), "while you're the monarch", and "a land with the same name as the imprinted card".
     */
    private boolean manaAbilityTriggers(TriggerMatchContext match, AddOneOfEachManaTypeProducedByLandEffect trigger,
                                        TriggerContext.LandTap lt) {
        if (trigger.controllerOnly() && !match.controllerId().equals(lt.tappingPlayerId())) return false;
        if (trigger.monarchOnly() && !match.controllerId().equals(match.gameData().monarchPlayerId)) return false;
        if (!trigger.matchesImprintedCardName()) return true;
        Card sourceCard = match.sourceCard() != null ? match.sourceCard()
                : match.permanent() != null ? match.permanent().getCard() : null;
        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (sourceCard == null || tappedLand == null) return false;
        Card imprintedCard = match.gameData().getImprintedCard(sourceCard);
        return imprintedCard != null
                && match.gameData().findExiledCard(imprintedCard.getId()) != null
                && imprintedCard.getName().equals(tappedLand.getCard().getName());
    }

    /**
     * Sasaya's Essence's "whenever a land you control is tapped for mana": a triggered mana ability resolved at once by
     * its effect's handler, which counts the other lands with the same name.
     */
    @CollectsTapTrigger(value = AddManaForEachOtherLandWithSameNameEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaForEachOtherLandWithSameName(TriggerMatchContext match,
            AddManaForEachOtherLandWithSameNameEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;
        resolveOrAwaitProducedMana(match.gameData(), landTapEntry(match, trigger, lt), lt, true);
        return true;
    }

    @CollectsTapTrigger(value = AddManaOfTypeProducedByTappedPermanentEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaOfTypeProducedByLand(TriggerMatchContext match,
            AddManaOfTypeProducedByTappedPermanentEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;

        if (lt.producedColors().isEmpty()
                && match.gameData().interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    lt.tappingPlayerId(),
                    match.permanent().getCard().getName() + "'s mana ability",
                    new ArrayList<>(List.of((CardEffect) trigger)),
                    null,
                    match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setAwaitingProducedManaColor(true);
            match.gameData().pendingManaAbilityTriggers.add(entry);
            return true;
        }

        ManaColor requiredColor = trigger.requiredColor();
        if (requiredColor == null || !lt.producedColors().contains(requiredColor)) {
            return false;
        }

        match.gameData().playerManaPools.get(lt.tappingPlayerId()).add(requiredColor);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(lt.tappingPlayerId())
                        + " adds 1 additional " + requiredColor.name().toLowerCase() + " mana."));
        return true;
    }

    @CollectsTapTrigger(value = AddManaWhenLandOfSubtypeTappedForManaEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaWhenSubtypeLandTapped(TriggerMatchContext match,
            AddManaWhenLandOfSubtypeTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (!gameQueryService.hasEffectiveSubtype(match.gameData(), tappedLand, trigger.subtype())) return false;
        if (trigger.controllerOnly() && !match.controllerId().equals(lt.tappingPlayerId())) return false;

        UUID recipientId = trigger.controllerOnly() ? lt.tappingPlayerId()
                : gameQueryService.findPermanentController(match.gameData(), tappedLand.getId());
        if (recipientId == null) return false;
        ManaPool pool = match.gameData().playerManaPools.get(recipientId);
        pool.add(trigger.color());

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(recipientId)
                        + " adds 1 additional " + trigger.color().name().toLowerCase() + " mana."));
        return true;
    }

    @CollectsTapTrigger(value = AddManaWhenLandTappedForManaEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaWhenLandTapped(TriggerMatchContext match,
            AddManaWhenLandTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (trigger.sourceOnly()
                && (match.permanent() == null || !match.permanent().getId().equals(lt.tappedLandId()))) {
            return false;
        }
        if (trigger.controllerOnly() && !match.controllerId().equals(lt.tappingPlayerId())) return false;

        var gameData = match.gameData();
        gameData.playerManaPools.get(lt.tappingPlayerId()).add(trigger.color());
        gameLogService.append(gameData, GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + gameData.playerIdToName.get(lt.tappingPlayerId())
                        + " adds 1 additional " + trigger.color().name().toLowerCase() + " mana."));
        return true;
    }

    @CollectsTapTrigger(value = AddManaWhenLandOfColorTappedForManaEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddManaWhenColorLandTapped(TriggerMatchContext match,
            AddManaWhenLandOfColorTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (trigger.controllerOnly() && !match.controllerId().equals(lt.tappingPlayerId())) return false;

        var gameData = match.gameData();
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, lt.tappedLandId());
        boolean producedThisTap = lt.producedColors().contains(trigger.color());
        if (tappedLand == null || !(producedThisTap || landProducesManaColor(gameData, tappedLand, trigger.color()))) {
            return false;
        }

        gameData.playerManaPools.get(lt.tappingPlayerId()).add(trigger.color());
        gameLogService.append(gameData, GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + gameData.playerIdToName.get(lt.tappingPlayerId())
                        + " adds 1 additional " + trigger.color().name().toLowerCase() + " mana."));
        return true;
    }

    private boolean landProducesManaColor(com.github.laxika.magicalvibes.model.GameData gameData,
                                           Permanent land, ManaColor color) {
        return tapManaAbilityProducesColor(ManaAbilities.intrinsic(land.getCard()), color)
                || tapManaAbilityProducesColor(
                        gameQueryService.computeStaticBonus(gameData, land).grantedActivatedAbilities(), color);
    }

    private static boolean tapManaAbilityProducesColor(List<ActivatedAbility> abilities, ManaColor color) {
        return abilities.stream()
                .filter(ActivatedAbility::isRequiresTap)
                .filter(ActivatedAbility::isManaAbility)
                .flatMap(ability -> ability.getEffects().stream())
                .filter(ManaProducingEffect.class::isInstance)
                .map(ManaProducingEffect.class::cast)
                .anyMatch(mana -> mana.estimatedManaColor() == color);
    }

    @CollectsTapTrigger(value = AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleAddRestrictedManaWhenSubtypeLandTapped(TriggerMatchContext match,
            AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;
        if (!gameQueryService.effectiveBasicLandTypes(match.gameData(), tappedLand)
                .contains(trigger.subtype())) return false;

        boolean snow = gameQueryService.hasEffectiveSupertype(match.gameData(), tappedLand, CardSupertype.SNOW);
        int amount = snow ? trigger.snowAmount() : trigger.amount();
        UUID landControllerId = gameQueryService.findPermanentController(
                match.gameData(), tappedLand.getId());
        if (landControllerId == null) return false;
        ManaPool pool = match.gameData().playerManaPools.get(landControllerId);
        trigger.restriction().applyTo(pool, trigger.color(), amount);

        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(landControllerId)
                        + " adds " + amount + " additional " + trigger.color().name().toLowerCase()
                        + " mana (" + trigger.restriction().description() + ")."));
        return true;
    }

    @CollectsTapTrigger(value = ReturnTappedLandToHandEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleReturnTappedLandToHand(TriggerMatchContext match,
            ReturnTappedLandToHandEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) return false;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(ReturnToHandEffect.triggering())),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(tappedLand.getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsTapTrigger(value = DestroyReferencedPermanentEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
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
    @CollectsTapTrigger(value = CreateTokenForTargetPlayerEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleCreateTokenForOpponentOnSelfTapped(TriggerMatchContext match,
            CreateTokenForTargetPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.permanent().getId().equals(lt.tappedLandId())) return false;
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;

        UUID opponentId = gameQueryService.getOpponentId(match.gameData(), match.controllerId());
        if (opponentId == null) return false;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                opponentId,
                match.permanent().getId());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsTapTrigger(value = RegisterDelayedChooseOpponentGainsControlOfSourceEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleDelayedControlChangeOnSelfTapped(TriggerMatchContext match,
            RegisterDelayedChooseOpponentGainsControlOfSourceEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (!match.permanent().getId().equals(lt.tappedLandId())) return false;
        if (!match.controllerId().equals(lt.tappingPlayerId())) return false;

        // The delayed trigger is created by the mana ability itself, which does not use the stack.
        match.gameData().queueDelayedAction(new DelayedChooseOpponentGainsControlOfSource(
                match.controllerId(), match.permanent().getId(), match.permanent().getCard()));
        return true;
    }

    @CollectsTapTrigger(value = TapPermanentsEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleTapOpponentLands(TriggerMatchContext match,
            TapPermanentsEffect trigger, TriggerContext ctx) {
        TriggerContext.LandTap lt = (TriggerContext.LandTap) ctx;
        if (trigger.scope() != TapUntapScope.TARGET_PLAYERS_PERMANENTS
                || match.controllerId().equals(lt.tappingPlayerId())) {
            return false;
        }

        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null || !gameQueryService.isLand(match.gameData(), tappedLand)) {
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
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to tap all lands controlled by the opponent who tapped a land",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * Queues Mana Web's triggered ability. The tapping player is stored as a non-targeting player
     * reference, while the tapped land is stored as the triggering permanent so both are available
     * when the ability resolves after players have had priority.
     */
    @CollectsTapTrigger(value = TapLandsThatCouldProduceSameManaAsTappedLandEffect.class,
            watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
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
        Permanent tappedLand = gameQueryService.findPermanentById(match.gameData(), lt.tappedLandId());
        if (tappedLand == null) {
            // The land left the battlefield as its mana ability resolved (Gemstone Mine's final use):
            // the trigger still uses what it could produce as it last existed.
            tappedLand = match.gameData().departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> snapshot.getId().equals(lt.tappedLandId()))
                    .findFirst().orElse(null);
        }
        if (tappedLand != null) {
            entry.rememberLastKnownPermanentCard(tappedLand.getId(), tappedLand.getCard());
        }
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on opponent land tap", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

}
