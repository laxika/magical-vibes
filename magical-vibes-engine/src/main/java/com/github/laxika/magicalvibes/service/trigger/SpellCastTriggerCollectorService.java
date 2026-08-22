package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSameNameCardFromGraveyardOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedPermanentSelfTargetingEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromGraveyardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachPriorInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetedSpellPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfNameFoundElsewhereEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellingEffect;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentFirstSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToManaSpentToCastToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAllCardsWithCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsWithCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.FirstMulticoloredSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinCopyTriggeringSpellOrDealDamageEffect;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.PossibilityStormCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PossibilityStormExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandAndDiscardMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.RashmiRevealTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.RashmiTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSubtypeSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastLifeDrainEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellweaverHelixTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StormCopyEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentGreaterThanSourcePower;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationRevealAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
/**
 * Trigger collectors for spell-cast events (ON_ANY_PLAYER_CASTS_SPELL,
 * ON_CONTROLLER_CASTS_SPELL, ON_OPPONENT_CASTS_SPELL).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpellCastTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final TargetLegalityService targetLegalityService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ValidTargetService validTargetService;

    // ── ON_ANY_PLAYER_CASTS_SPELL ──────────────────────────────────────

    @CollectsTrigger(value = SpellCastTriggerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleAnyPlayerSpellCastTrigger(TriggerMatchContext match, SpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return handleGenericSpellCastTrigger(match, trigger, sc.spellCard(), sc.castingPlayerId());
    }

    @CollectsTrigger(value = FirstMulticoloredSpellCastTriggerEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleFirstMulticoloredSpellCastTrigger(TriggerMatchContext match,
            FirstMulticoloredSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (sc.spellCard().getColors().size() < 2) {
            return false;
        }
        long multicoloredSpellsThisTurn = match.gameData().getSpellsCastThisTurn(sc.castingPlayerId()).stream()
                .filter(card -> card.getColors().size() >= 2)
                .count();
        if (multicoloredSpellsThisTurn != 1) {
            return false;
        }
        return handleGenericSpellCastTrigger(match,
                new SpellCastTriggerEffect(null, trigger.resolvedEffects()),
                sc.spellCard(), sc.castingPlayerId());
    }

    @CollectsTrigger(value = SpellweaverHelixTriggerEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleSpellweaverHelixTrigger(TriggerMatchContext match,
                                                   SpellweaverHelixTriggerEffect trigger,
                                                   TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry castEntry = findStackEntryForCard(match.gameData(), sc.spellCard().getId());
        if (castEntry == null || castEntry.isCopy()) {
            return false;
        }

        List<Card> exiledCards = match.gameData().getCardsExiledByPermanent(match.permanent().getId());
        if (exiledCards.size() < 2
                || exiledCards.stream().limit(2).noneMatch(card -> card.getName().equals(sc.spellCard().getName()))) {
            return false;
        }

        StackEntry triggerEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new MayEffect(
                        CopyImprintedCardAndMayCastCopyEffect.otherExiledCard(sc.spellCard().getId()),
                        "You may copy the other exiled card and cast it without paying its mana cost?"
                ))),
                null,
                match.permanent().getId()
        );
        triggerEntry.setNonTargeting(true);
        match.gameData().stack.add(triggerEntry);
        return true;
    }

    @CollectsTrigger(value = CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCreateSquirrelTokensForSameNameCardsOnSpellCast(
            TriggerMatchContext match, CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        CreateTokenEffect token = new CreateTokenEffect(
                new CardsInGraveyard(new CardNamedPredicate(sc.spellCard().getName()), CountScope.ANY_PLAYER),
                "Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL), Set.of(), Set.of());
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new CreateTokenForTargetPlayerEffect(token))),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleDamageForSameNameCardsOnSpellCast(
            TriggerMatchContext match, DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        CardEffect damage = new DealDamageToPlayersEffect(
                new Scaled(new CardsInGraveyard(
                        new CardNamedPredicate(sc.spellCard().getName()), CountScope.ANY_PLAYER), 2),
                DamageRecipient.TRIGGERING_PLAYER);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleGainLifeForSameNameCardsOnSpellCast(
            TriggerMatchContext match, GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        CardEffect lifeGain = new GainLifeEffect(
                new CardsInGraveyard(new CardNamedPredicate(sc.spellCard().getName()), CountScope.ANY_PLAYER),
                GainLifeRecipient.TRIGGERING_PLAYER);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(lifeGain)),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCounterUnlessPaysForSameNameCardsOnSpellCast(
            TriggerMatchContext match,
            CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        CounterUnlessPaysEffect counterUnlessPays = new CounterUnlessPaysEffect(
                new CardsInGraveyard(new CardNamedPredicate(sc.spellCard().getName()), CountScope.ANY_PLAYER));
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(counterUnlessPays)),
                sc.spellCard().getId(),
                Zone.STACK));
        return true;
    }

    @CollectsTrigger(value = CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCounterUnlessOtherPlayerPaysManaCostOnSpellCast(
            TriggerMatchContext match,
            CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry spellEntry = findStackEntryForCard(match.gameData(), sc.spellCard().getId());
        String manaCost = sc.spellCard().getManaCost();
        if (spellEntry == null || manaCost == null) {
            return false;
        }

        manaCost = manaCost.replace("{X}", "{" + spellEntry.getXValue() + "}");
        MayPayManaEffect payAndCounter = new MayPayManaEffect(
                manaCost,
                new CounterSpellEffect(),
                "Pay " + manaCost + " to counter " + sc.spellCard().getName() + "?",
                MayPayPayer.ANY_OTHER_PLAYER);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(payAndCounter)),
                sc.spellCard().getId(),
                Zone.STACK,
                match.permanent().getId());
        entry.setActivePlayerId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DiscardForSameNameCardsInGraveyardsOnSpellCastEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleDiscardForSameNameCardsOnSpellCast(
            TriggerMatchContext match, DiscardForSameNameCardsInGraveyardsOnSpellCastEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        DiscardEffect discard = new DiscardEffect(
                new CardsInGraveyard(new CardNamedPredicate(sc.spellCard().getName()), CountScope.ANY_PLAYER),
                DiscardRecipient.TARGET_PLAYER);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(discard)));
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleAnyPlayerColorCounter(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!trigger.matchesColor(sc.spellCard().getColor())) return false;
        if (trigger.onlyOwnSpells()) return false;
        return addColorCounterTrigger(match, trigger);
    }

    @CollectsTrigger(value = CounterSpellIfManaValueEqualsSourceCountersEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCounterSpellIfManaValueEqualsSourceCounters(
            TriggerMatchContext match,
            CounterSpellIfManaValueEqualsSourceCountersEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry spellEntry = findStackEntryForCard(match.gameData(), sc.spellCard().getId());
        if (spellEntry == null || !targetLegalityService.matchesStackEntryPredicate(
                match.gameData(), spellEntry,
                new StackEntryManaValueEqualsSourceCountersPredicate(trigger.counterType()),
                match.controllerId(), match.permanent())) {
            return false;
        }

        int manaValue = spellEntry.getCard().getManaValue() + spellEntry.getXValue();
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new CounterSpellIfManaValueEqualsSourceCountersEffect(
                        trigger.counterType(), manaValue))),
                sc.spellCard().getId(),
                Zone.STACK
        ));
        return true;
    }

    @CollectsTrigger(value = KnowledgePoolCastTriggerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleKnowledgePoolCast(TriggerMatchContext match,
            KnowledgePoolCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!sc.castFromHand()) return false;

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new KnowledgePoolExileAndCastEffect(
                        sc.spellCard().getId(), match.permanent().getId(), sc.castingPlayerId())))
        ));
        return true;
    }

    @CollectsTrigger(value = PossibilityStormCastTriggerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handlePossibilityStormCast(TriggerMatchContext match,
            PossibilityStormCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!sc.castFromHand()) return false;

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new PossibilityStormExileAndCastEffect(
                        sc.spellCard().getId(), match.permanent().getId(), sc.castingPlayerId())))
        ));
        return true;
    }

    @CollectsTrigger(value = CopySpellForEachOtherSubtypePermanentEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCopySpellForSubtype(TriggerMatchContext match,
            CopySpellForEachOtherSubtypePermanentEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellSnapshot() != null) return false;

        StackEntry spellEntry = findInstantOrSorceryOnStack(match, sc);
        UUID singleTargetId = soleNonPlayerTargetId(match.gameData(), spellEntry);
        if (singleTargetId == null) return false;

        Permanent targetPerm = gameQueryService.findPermanentById(match.gameData(), singleTargetId);
        if (targetPerm == null) return false;
        if (!targetPerm.getCard().getSubtypes().contains(trigger.subtype())) return false;

        StackEntry snapshot = new StackEntry(spellEntry);
        CopySpellForEachOtherSubtypePermanentEffect resolutionEffect =
                new CopySpellForEachOtherSubtypePermanentEffect(
                        trigger.subtype(), snapshot, sc.castingPlayerId(), singleTargetId);

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolutionEffect))
        ));
        return true;
    }

    @CollectsTrigger(value = CopySpellForEachOtherControlledCreatureEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    @CollectsTrigger(value = CopySpellForEachOtherControlledCreatureEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleCopySpellForEachOtherControlledCreature(TriggerMatchContext match,
            CopySpellForEachOtherControlledCreatureEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellSnapshot() != null) return false;

        StackEntry spellEntry = findInstantOrSorceryOnStack(match, sc);
        UUID singleTargetId = soleNonPlayerTargetId(match.gameData(), spellEntry);
        if (singleTargetId == null) return false;
        // Mirrorwing style: spell must target only this source permanent
        if (!singleTargetId.equals(match.permanent().getId())) return false;
        if (trigger.chooseOne() && !hasOtherLegalCreatureTarget(
                match.gameData(), spellEntry.getCard(), sc.castingPlayerId(), singleTargetId)) return false;

        StackEntry snapshot = new StackEntry(spellEntry);
        CopySpellForEachOtherControlledCreatureEffect resolutionEffect =
                new CopySpellForEachOtherControlledCreatureEffect(
                        snapshot, sc.castingPlayerId(), singleTargetId, trigger.chooseOne());

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolutionEffect))
        ));
        return true;
    }

    private boolean hasOtherLegalCreatureTarget(GameData gameData, Card spellCard,
                                                UUID castingPlayerId, UUID originalTargetId) {
        return gameData.playerBattlefields.getOrDefault(castingPlayerId, List.of()).stream()
                .anyMatch(permanent -> !permanent.getId().equals(originalTargetId)
                        && gameQueryService.isCreature(gameData, permanent)
                        && validTargetService.canPermanentBeTargetedBySpell(
                                gameData, permanent, spellCard, castingPlayerId));
    }

    private StackEntry findInstantOrSorceryOnStack(TriggerMatchContext match, TriggerContext.SpellCast sc) {
        Card spellCard = sc.spellCard();
        if (!spellCard.hasType(CardType.INSTANT) && !spellCard.hasType(CardType.SORCERY)) return null;
        for (StackEntry se : match.gameData().stack) {
            if (se.getCard().getId().equals(spellCard.getId())) {
                return se;
            }
        }
        return null;
    }

    /** Exactly one unique non-player target on the spell, else null. */
    private UUID soleNonPlayerTargetId(GameData gameData, StackEntry spellEntry) {
        if (spellEntry == null) return null;

        UUID singleTargetId = null;
        if (spellEntry.getTargetId() != null
                && spellEntry.getTargetZone() == null
                && spellEntry.getTargetIds().isEmpty()) {
            singleTargetId = spellEntry.getTargetId();
        } else if (spellEntry.getTargetId() == null
                && !spellEntry.getTargetIds().isEmpty()
                && spellEntry.getTargetIds().stream().distinct().count() == 1) {
            singleTargetId = spellEntry.getTargetIds().getFirst();
        }
        if (singleTargetId == null) return null;
        if (gameData.playerIds.contains(singleTargetId)) return null;
        return singleTargetId;
    }

    @CollectsTrigger(value = CopySpellForEachOtherPlayerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCopySpellForEachOtherPlayer(TriggerMatchContext match,
            CopySpellForEachOtherPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellSnapshot() != null) return false;

        // Find the spell on the stack
        StackEntry spellEntry = null;
        for (StackEntry se : match.gameData().stack) {
            if (se.getCard().getId().equals(sc.spellCard().getId())) {
                spellEntry = se;
                break;
            }
        }
        if (spellEntry == null) return false;

        // The spell filter fully expresses what triggers the copy — instant/sorcery type, plus
        // (for Curse of Echoes) controlled-by-the-enchanted-player. Evaluated against the cast
        // spell's stack entry, with the source aura's attachedTo as the enchanted-player context.
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesStackEntryPredicate(spellEntry, trigger.spellFilter(),
                        match.permanent().getAttachedTo())) {
            return false;
        }

        StackEntry snapshot = new StackEntry(spellEntry);
        CopySpellForEachOtherPlayerEffect resolutionEffect =
                new CopySpellForEachOtherPlayerEffect(snapshot, sc.castingPlayerId(), trigger.optional());

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolutionEffect))
        ));
        return true;
    }

    @CollectsTrigger(value = CasterLosesLifeOnSpellCastEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    @CollectsTrigger(value = CasterLosesLifeOnSpellCastEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    @CollectsTrigger(value = CasterLosesLifeOnSpellCastEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCasterLosesLifeOnSpellCast(TriggerMatchContext match,
            CasterLosesLifeOnSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // "that player" = the caster; preset the target so the loss falls on them, not a choice.
        int amount = trigger.useSpellManaValue() ? sc.spellCard().getManaValue() : trigger.amount();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new LoseLifeEffect(amount, LoseLifeRecipient.TARGET_PLAYER)))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = CasterLosesLifeOnChosenColorSpellCastEffect.class,
            slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCasterLosesLifeOnChosenColorSpellCast(TriggerMatchContext match,
            CasterLosesLifeOnChosenColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (match.permanent().getChosenColor() == null
                || !sc.spellCard().getColors().contains(match.permanent().getChosenColor())) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new LoseLifeEffect(trigger.amount(), LoseLifeRecipient.TARGET_PLAYER)))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = ReturnPermanentControlledByPlayerToHandEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleReturnLandOnSpellCast(TriggerMatchContext match,
            ReturnPermanentControlledByPlayerToHandEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "that player returns a land they control" — carry the casting player on targetId so the
        // resolution handler prompts them (not the enchantment's controller).
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)));
        // Mana Breach doesn't target — targetId only carries the acting (casting) player.
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DiscardEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleDiscardOnSpellCast(TriggerMatchContext match,
            DiscardEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "that player discards a card" — carry the casting player on targetId so the
        // TARGET_PLAYER discard lands on them (not the enchantment's controller). Oppression.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)));
        // Oppression doesn't target — targetId only carries the acting (casting) player.
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = CounterSpellIfNameFoundElsewhereEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCounterIfNameFoundElsewhere(TriggerMatchContext match,
            CounterSpellIfNameFoundElsewhereEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // Bazaar of Wonders: the name check is part of the effect, so it happens on resolution.
        // The cast spell is stamped as the trigger's target (auto-chosen, not player-selected).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.spellCard().getId(),
                Zone.STACK));
        return true;
    }

    // ── ON_CONTROLLER_CASTS_SPELL ──────────────────────────────────────

    @CollectsTrigger(value = PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleControllerColorCounter(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!trigger.matchesColor(sc.spellCard().getColor())) return false;
        return addColorCounterTrigger(match, trigger);
    }

    @CollectsTrigger(value = PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleControllerChosenColorCounter(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (match.permanent().getChosenColor() == null
                || !sc.spellCard().getColors().contains(match.permanent().getChosenColor())) {
            return false;
        }
        return addColorCounterTrigger(match, trigger.amount());
    }

    @CollectsTrigger(value = SpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleControllerSpellCastTrigger(TriggerMatchContext match, SpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return handleGenericSpellCastTrigger(match, trigger, sc.spellCard(), sc.castingPlayerId());
    }

    @CollectsTrigger(value = CastSameNameCardFromGraveyardOnSpellCastEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleCastSameNameCardFromGraveyard(TriggerMatchContext match,
            CastSameNameCardFromGraveyardOnSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (sc.castZone() != Zone.HAND
                || (!sc.spellCard().hasType(CardType.INSTANT)
                && !sc.spellCard().hasType(CardType.SORCERY))) {
            return false;
        }

        CardEffect castEffect = new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                false,
                false,
                new CardNamedPredicate(sc.spellCard().getName()));
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(castEffect)
        ));
        log.info("Game {} - {} same-name graveyard spell-cast trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = GainLifeForEachChosenColorSpellCastEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleGainLifeForEachChosenColor(TriggerMatchContext match,
            GainLifeForEachChosenColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        int lifeGain = (int) sc.spellCard().getColors().stream()
                .filter(match.permanent().getChosenColors()::contains)
                .count();
        if (lifeGain == 0) {
            return false;
        }

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new GainLifeEffect(lifeGain))),
                null,
                match.permanent().getId()
        ));
        return true;
    }

    @CollectsTrigger(value = BoostEquippedCreatureUntilEndOfTurnEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleBoostEquippedOnSpellCast(TriggerMatchContext match,
            BoostEquippedCreatureUntilEndOfTurnEffect trigger, TriggerContext ctx) {
        // "Whenever you cast a spell, equipped creature gets +X/+Y until end of turn" (Leering Emblem).
        // Carry the source permanent id so the handler can find the equipment and its equipped creature
        // (the effect fizzles at resolution if the Equipment is no longer attached).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()
        ));
        log.info("Game {} - {} spell-cast equipped-boost trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = CopyControllerCastSpellOnSpellCastEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleCopyControllerCastSpellOnSpellCast(TriggerMatchContext match,
            CopyControllerCastSpellOnSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;

        if (trigger.requiredCastZone() != null && sc.castZone() != trigger.requiredCastZone()) {
            return false;
        }

        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(),
                match.permanent().getCard().getId(),
                match.gameData(), sc.castingPlayerId())) {
            return false;
        }

        if (trigger.intervening() != null && !conditionEvaluationService.isMet(match.gameData(),
                trigger.intervening(), ConditionContext.forPermanent(match.permanent(), match.controllerId()))) {
            return false;
        }

        StackEntry spellEntry = null;
        for (StackEntry se : match.gameData().stack) {
            if (se.getCard().getId().equals(sc.spellCard().getId())) {
                spellEntry = se;
                break;
            }
        }
        if (spellEntry == null) return false;

        if (trigger.castSpellTargetCondition() != null
                && !targetLegalityService.matchesStackEntryPredicate(match.gameData(), spellEntry,
                trigger.castSpellTargetCondition(), match.controllerId(), match.permanent())) {
            return false;
        }

        StackEntry snapshot = new StackEntry(spellEntry);
        CardEffect copyEffect =
                new CopyControllerCastSpellEffect(snapshot, sc.castingPlayerId(), trigger.grantedKeywords(),
                        trigger.additionalTypes(), trigger.tokenCopy(), trigger.mayChooseNewTargets());
        if (trigger.intervening() != null) {
            copyEffect = new ConditionalEffect(trigger.intervening(), copyEffect);
        }

        // "you may copy that spell" with no cost (Swarm Intelligence) — offer an immediate optional
        // prompt; accepting puts the copy-creating ability on the stack.
        if (trigger.tapCost() == null && trigger.manaCost() == null && match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    match.permanent().getCard(),
                    match.controllerId(),
                    new ArrayList<>(List.of(copyEffect)),
                    match.permanent().getCard().getName() + " — " + may.prompt(),
                    null,
                    null,
                    match.permanent().getId()));
            return true;
        }

        CardEffect resolutionEffect;
        if (trigger.tapCost() != null) {
            resolutionEffect = new MayPayTapPermanentsEffect(
                    trigger.tapCost(),
                    copyEffect,
                    "Tap " + trigger.tapCost().count() + " untapped creatures you control to copy "
                            + sc.spellCard().getName() + "?");
        } else if (trigger.manaCost() != null) {
            resolutionEffect = new MayPayManaEffect(
                    trigger.manaCost(),
                    copyEffect,
                    "Pay " + trigger.manaCost() + " to copy " + sc.spellCard().getName() + "?");
        } else {
            resolutionEffect = copyEffect;
        }

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolutionEffect)),
                null,
                match.permanent().getId()
        ));
        return true;
    }

    @CollectsTrigger(value = CreateTokenCopyOfTargetedSpellPermanentEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleCreateTokenCopyOfTargetedSpellPermanent(TriggerMatchContext match,
            CreateTokenCopyOfTargetedSpellPermanentEffect ignored, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!sc.spellCard().hasType(CardType.INSTANT) && !sc.spellCard().hasType(CardType.SORCERY)) {
            return false;
        }
        StackEntry spellEntry = findStackEntryForCard(match.gameData(), sc.spellCard().getId());
        if (spellEntry == null) {
            return false;
        }
        boolean targetsOtherControlledPermanent = spellEntry.getDeclaredTargetIds().stream()
                .filter(targetId -> !targetId.equals(match.permanent().getId()))
                .map(targetId -> gameQueryService.findPermanentById(match.gameData(), targetId))
                .filter(java.util.Objects::nonNull)
                .anyMatch(target -> match.controllerId().equals(match.gameData().findControllerOf(target)));
        if (!targetsOtherControlledPermanent) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new CreateTokenCopyOfTargetedSpellPermanentEffect(
                        new StackEntry(spellEntry)))),
                null,
                match.permanent().getId()));
        return true;
    }

    @CollectsTrigger(value = CopySpellForEachPriorInstantOrSorceryEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleCopySpellForEachPriorInstantOrSorcery(TriggerMatchContext match,
            CopySpellForEachPriorInstantOrSorceryEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        Card spellCard = sc.spellCard();
        if (!spellCard.hasType(CardType.INSTANT) && !spellCard.hasType(CardType.SORCERY)) {
            return false;
        }

        StackEntry spellEntry = findStackEntryForCard(match.gameData(), spellCard.getId());
        if (spellEntry == null) {
            return false;
        }

        int copies = (int) match.gameData().getSpellsCastThisTurn(sc.castingPlayerId()).stream()
                .filter(card -> !card.getId().equals(spellCard.getId()))
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .count();
        StackEntry snapshot = new StackEntry(spellEntry);
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new StormCopyEffect(snapshot, sc.castingPlayerId(), copies)))
        ));
        return true;
    }

    @CollectsTrigger(value = ChosenSubtypeSpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleChosenSubtypeSpellCastTrigger(TriggerMatchContext match,
            ChosenSubtypeSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        CardSubtype chosenSubtype = match.permanent().getChosenSubtype();
        if (chosenSubtype == null) return false;

        // Must be a spell of the chosen type (optionally restricted to creature spells)
        CardPredicate subtypeFilter = trigger.creatureSpellOnly()
                ? new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(chosenSubtype)))
                : new CardSubtypePredicate(chosenSubtype);
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(),
                subtypeFilter, null, match.gameData(), sc.castingPlayerId())) return false;

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(trigger.resolvedEffects()),
                null,
                match.permanent().getId()
        ));

        log.info("Game {} - {} chosen-subtype spell-cast trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = NthSpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL),
            @CollectsTrigger(value = NthSpellCastTriggerEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL),
            @CollectsTrigger(value = NthSpellCastTriggerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    })
    private boolean handleNthSpellCastTrigger(TriggerMatchContext match, NthSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        int spellsCast = trigger.countScope() == CountScope.ANY_PLAYER
                ? match.gameData().getTotalSpellsCastThisTurnCount()
                : match.gameData().getSpellsCastThisTurnCount(sc.castingPlayerId());
        if (spellsCast != trigger.spellNumber()) return false;

        List<CardEffect> resolved = new ArrayList<>(trigger.resolvedEffects());
        boolean selfTarget = resolved.stream().anyMatch(e -> e.targetSpec().selfTargeting());

        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolved,
                    match.permanent().getCard().getName() + " — " + may.prompt(),
                    null,
                    null,
                    match.permanent().getId()));
        } else {
            StackEntry entry = selfTarget
                    ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                        match.permanent().getCard().getName() + "'s ability", resolved, null, match.permanent().getId())
                    : new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                        match.permanent().getCard().getName() + "'s ability", resolved);
            match.gameData().stack.add(entry);
        }

        log.info("Game {} - {} Nth-spell-cast trigger fired (spell #{})",
                match.gameData().id, match.permanent().getCard().getName(), trigger.spellNumber());
        return true;
    }

    @CollectsTrigger(value = CounterOpponentFirstSpellEachTurnEffect.Marker.class,
            slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCounterOpponentFirstSpell(TriggerMatchContext match,
                                                     CounterOpponentFirstSpellEachTurnEffect.Marker trigger,
                                                     TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (match.gameData().getSpellsCastThisTurnCount(sc.castingPlayerId()) != 1) return false;

        Card source = match.permanent().getCard();
        String description = source.getName() + "'s ability";
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source,
                match.controllerId(),
                description,
                new ArrayList<>(List.of(new CounterSpellEffect())),
                sc.spellCard().getId(),
                Zone.STACK
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(source, "'s ability triggers — counter that spell."));
        log.info("Game {} - {} counters opponent's first spell this turn",
                match.gameData().id, source.getName());
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = KickedSpellCastTriggerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL),
            @CollectsTrigger(value = KickedSpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    })
    private boolean handleKickedSpellCastTrigger(TriggerMatchContext match,
            KickedSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;

        StackEntry spellEntry = findStackEntryForCard(match.gameData(), sc.spellCard().getId());
        if (spellEntry == null) return false;
        int kickCount = kickedCount(spellEntry);
        if (kickCount == 0) return false;

        List<CardEffect> resolved = new ArrayList<>(trigger.resolvedEffects());

        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolved,
                    match.permanent().getCard().getName() + " â€” " + may.prompt(),
                    null,
                    null,
                    match.permanent().getId(),
                    null,
                    0,
                    0,
                    null,
                    null,
                    null,
                    null,
                    null,
                    kickCount));
            return true;
        }

        // The trigger's source permanent is always carried on the entry — source-relative
        // effects (put counters on source, damage equal to counters on source) need it.
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                match.permanent().getCard().getName() + "'s ability", resolved, null, match.permanent().getId());
        entry.setEventValue(kickCount);
        match.gameData().stack.add(entry);

        log.info("Game {} - {} kicked-spell-cast trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    private int kickedCount(StackEntry spellEntry) {
        int count = spellEntry.isKicked() ? 1 : 0;
        boolean isMultikicker = spellEntry.getCard().getEffects(EffectSlot.SPELL).stream()
                .filter(RepeatableAdditionalManaCost.class::isInstance)
                .map(RepeatableAdditionalManaCost.class::cast)
                .anyMatch(RepeatableAdditionalManaCost::multikicker);
        if (isMultikicker) {
            count += spellEntry.getRepeatedAdditionalCosts().size();
        }
        return count;
    }

    @CollectsTrigger(value = CastFromGraveyardTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleCastFromGraveyardTrigger(TriggerMatchContext match,
            CastFromGraveyardTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (sc.castZone() != Zone.GRAVEYARD) return false;

        boolean needsAnyTarget = trigger.resolvedEffects().stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER) || e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));

        if (needsAnyTarget) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(trigger.resolvedEffects())
            ));
            gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers — choose a target."));
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(trigger.resolvedEffects())
            ));
        }
        log.info("Game {} - {} cast-from-graveyard trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = SunbirdsInvocationTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleSunbirdsInvocationCast(TriggerMatchContext match,
            SunbirdsInvocationTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!sc.castFromHand()) return false;

        int manaValue = sc.spellCard().getManaValue();

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new SunbirdsInvocationRevealAndCastEffect(manaValue)))
        ));

        log.info("Game {} - Sunbird's Invocation trigger queued (mana value {})",
                match.gameData().id, manaValue);
        return true;
    }

    @CollectsTrigger(value = RashmiTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleRashmiFirstSpell(TriggerMatchContext match,
            RashmiTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (match.gameData().getSpellsCastThisTurnCount(sc.castingPlayerId()) != 1) return false;

        int manaValue = spellManaValue(match.gameData(), sc.spellCard());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new RashmiRevealTopCardEffect(manaValue))),
                null,
                match.permanent().getId()
        ));
        log.info("Game {} - {} first-spell trigger queued (mana value {})",
                match.gameData().id, match.permanent().getCard().getName(), manaValue);
        return true;
    }

    @CollectsTrigger(value = DealDamageEqualToSpellManaValueToAnyTargetEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaValueDamage(TriggerMatchContext match,
            DealDamageEqualToSpellManaValueToAnyTargetEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = sc.spellCard().getManaValue();
        List<CardEffect> resolvedEffects = List.of(new DealDamageToAnyTargetEffect(manaValue));
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects)
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s triggered ability triggers — choose a target for " + manaValue + " damage."));
        log.info("Game {} - {} spell-cast mana-value trigger queued ({} damage)",
                match.gameData().id, match.permanent().getCard().getName(), manaValue);
        return true;
    }

    @CollectsTrigger(value = GainControlOfTargetCreatureByCastSpellManaValueEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleGainControlByCastSpellManaValue(
            TriggerMatchContext match, GainControlOfTargetCreatureByCastSpellManaValueEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = spellManaValue(match.gameData(), sc.spellCard());
        var targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentMinManaValuePredicate(manaValue),
                new PermanentMaxManaValuePredicate(manaValue)));
        List<CardEffect> resolvedEffects = List.of(
                GainControlOfTargetEffect.withTargetPredicate(ControlDuration.END_OF_TURN, targetPredicate));
        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(PendingMayAbility.forSpellCastTrigger(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolvedEffects,
                    match.permanent().getCard().getName() + " - " + may.prompt(),
                    null,
                    match.permanent().getId(),
                    sc.spellCard().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        } else {
            TargetFilter targetFilter = new PermanentPredicateTargetFilter(targetPredicate,
                    "Target must be a creature with mana value " + manaValue);
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects),
                    false, targetFilter, 0, match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers - choose a creature with mana value " + manaValue + "."));
        }
        return true;
    }

    @CollectsTrigger(value = MillTargetPlayerByCastSpellManaValueEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaValueMill(TriggerMatchContext match,
            MillTargetPlayerByCastSpellManaValueEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = spellManaValue(match.gameData(), sc.spellCard());
        List<CardEffect> resolvedEffects = List.of(new MillEffect(manaValue, MillRecipient.TARGET_PLAYER));
        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(PendingMayAbility.forSpellCastTrigger(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolvedEffects,
                    match.permanent().getCard().getName() + " - " + may.prompt(),
                    null,
                    match.permanent().getId(),
                    sc.spellCard().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        } else {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects)
            ));
            gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers - choose a player to mill " + manaValue + " cards."));
        }
        return true;
    }

    @CollectsTrigger(value = GainLifeEqualToCastSpellManaValueEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaValueLifeGain(TriggerMatchContext match,
            GainLifeEqualToCastSpellManaValueEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = spellManaValue(match.gameData(), sc.spellCard());
        List<CardEffect> resolvedEffects = List.of(new GainLifeEffect(manaValue));
        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(PendingMayAbility.forSpellCastTrigger(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolvedEffects,
                    match.permanent().getCard().getName() + " - " + may.prompt(),
                    null,
                    match.permanent().getId(),
                    sc.spellCard().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    resolvedEffects,
                    null,
                    match.permanent().getId()));
            log.info("Game {} - {} spell-cast mana-value life-gain trigger queued ({})",
                    match.gameData().id, match.permanent().getCard().getName(), manaValue);
        }
        return true;
    }

    @CollectsTrigger(value = DiscardAllCardsWithCastSpellManaValueEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaValueDiscard(TriggerMatchContext match,
            DiscardAllCardsWithCastSpellManaValueEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = spellManaValue(match.gameData(), sc.spellCard());
        CardPredicate matchingManaValue = new CardAllOfPredicate(List.of(
                new CardMinManaValuePredicate(manaValue),
                new CardMaxManaValuePredicate(manaValue)));
        List<CardEffect> resolvedEffects = List.of(
                new RevealHandAndDiscardMatchingCardsEffect(matchingManaValue));
        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(PendingMayAbility.forSpellCastTrigger(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolvedEffects,
                    match.permanent().getCard().getName() + " - " + may.prompt(),
                    null,
                    match.permanent().getId(),
                    sc.spellCard().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        } else {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects), true));
            gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers - choose a player to reveal their hand and discard cards with mana value "
                            + manaValue + "."));
        }
        return true;
    }

    @CollectsTrigger(value = DestroyAllPermanentsWithCastSpellManaValueEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleDestroyAllPermanentsWithCastSpellManaValue(
            TriggerMatchContext match, DestroyAllPermanentsWithCastSpellManaValueEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = spellManaValue(match.gameData(), sc.spellCard());
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                List.of(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentMinManaValuePredicate(manaValue),
                        new PermanentMaxManaValuePredicate(manaValue)
                )))),
                null,
                match.permanent().getId());
        match.gameData().stack.add(entry);
        log.info("Game {} - {} spell-cast mana-value destruction trigger queued ({})",
                match.gameData().id, match.permanent().getCard().getName(), manaValue);
        return true;
    }

    @CollectsTrigger(value = DealDamageEqualToManaSpentToCastToAnyTargetEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaSpentDamage(TriggerMatchContext match,
            DealDamageEqualToManaSpentToCastToAnyTargetEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) {
            return false;
        }

        int manaSpent = match.gameData().getSpellCastManaSpent(sc.spellCard().getId());
        List<CardEffect> resolvedEffects = List.of(new DealDamageToAnyTargetEffect(manaSpent));
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects)
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s triggered ability triggers — choose a target for " + manaSpent + " damage."));
        log.info("Game {} - {} spell-cast mana-spent trigger queued ({} damage)",
                match.gameData().id, match.permanent().getCard().getName(), manaSpent);
        return true;
    }

    @CollectsTrigger(value = BoostSelfByCastSpellManaValueEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaValueSelfBoost(TriggerMatchContext match,
            BoostSelfByCastSpellManaValueEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = sc.spellCard().getManaValue();
        int toughnessBoost = trigger.boostToughness() ? manaValue : 0;
        List<CardEffect> resolved = new ArrayList<>(List.of(new BoostSelfEffect(manaValue, toughnessBoost)));
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                resolved,
                null,
                match.permanent().getId()));
        log.info("Game {} - {} spell-cast mana-value self-boost trigger queued (+{}/+{})",
                match.gameData().id, match.permanent().getCard().getName(), manaValue, manaValue);
        return true;
    }

    @CollectsTrigger(value = BecomeCreatureByCastSpellManaValueEffect.class,
            slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleBecomeCreatureByCastSpellManaValue(TriggerMatchContext match,
            BecomeCreatureByCastSpellManaValueEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!gameQueryService.isEnchantment(match.gameData(), match.permanent())) return false;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = sc.spellCard().getManaValue();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                List.of(new BecomeCreatureEffect(manaValue, manaValue, CardSubtype.ILLUSION)),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(sc.spellCard().getId());
        match.gameData().stack.add(entry);

        log.info("Game {} - {} spell-cast mana-value animation trigger queued ({} / {})",
                match.gameData().id, match.permanent().getCard().getName(), manaValue, manaValue);
        return true;
    }

    @CollectsTrigger(value = GivePoisonCountersEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handlePoisonOnSpellCast(TriggerMatchContext match,
            GivePoisonCountersEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() == null) return false;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        List<CardEffect> resolvedEffects = List.of(new GivePoisonCountersEffect(trigger.amount(), PoisonRecipient.TARGET_PLAYER));
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects), true
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s triggered ability triggers — choose target player for poison counter."));
        log.info("Game {} - {} spell-cast poison trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    // ── ON_OPPONENT_CASTS_SPELL ────────────────────────────────────────

    @CollectsTrigger(value = SpellCastTriggerEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleOpponentSpellCastTrigger(TriggerMatchContext match, SpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return handleGenericSpellCastTrigger(match, trigger, sc.spellCard(), sc.castingPlayerId());
    }

    @CollectsTrigger(value = ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect.class,
            slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleExileTopCardOfTriggeringPlayerLibraryAndMayCastFree(
            TriggerMatchContext match, ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (match.gameData().getSpellsCastThisTurnCount(sc.castingPlayerId()) != 1) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)));
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        entry.setTriggeringCardId(sc.spellCard().getId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSourceEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleOpponentSpellCastPutCountersOnSource(TriggerMatchContext match,
            PutCountersOnSourceEffect trigger, TriggerContext ctx) {
        // "Whenever an opponent casts a spell, put a counter on this creature" (Ammit Eternal, -1/-1).
        // Mandatory — carry the source permanent id so the counter effect knows which permanent to
        // modify (the generic SpellCastTriggerEffect path only binds the source for "may" abilities).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()
        ));
        log.info("Game {} - {} opponent-spell-cast put-counters trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = LoseLifeUnlessDiscardEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleLoseLifeUnlessDiscard(TriggerMatchContext match,
            LoseLifeUnlessDiscardEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleDamageToCastingOpponent(TriggerMatchContext match,
            DealDamageToPlayersEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DrawCardForTargetPlayerEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCastingOpponentDraws(TriggerMatchContext match,
            DrawCardForTargetPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = MillEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCastingOpponentMills(TriggerMatchContext match,
            MillEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "That player mills N cards" — carry the casting opponent on targetId so the
        // TARGET_PLAYER mill lands on them (not a chosen target). Memory Erosion.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = CounterUnlessPaysEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCounterUnlessPays(TriggerMatchContext match,
            CounterUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.spellCard().getId(),
                Zone.STACK
        );
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = RevealTopCardCreatureToBattlefieldOrMayBottomEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleRevealTopCardCreatureToBattlefield(TriggerMatchContext match,
            RevealTopCardCreatureToBattlefieldOrMayBottomEffect trigger, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        ));
        return true;
    }

    @CollectsTrigger(value = LoseLifeUnlessPaysEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleLoseLifeUnlessPays(TriggerMatchContext match,
            LoseLifeUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DamageUnlessPaysEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleDamageUnlessPays(TriggerMatchContext match,
            DamageUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // "that player" = the casting opponent (target of the damage / decision maker). Carry the source
        // permanent so the damage resolves with the correct source (prevention keys off it).
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DrawCardUnlessPaysEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleDrawCardUnlessPays(TriggerMatchContext match,
            DrawCardUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // Casting opponent is stamped on targetId (pay decision); controller draws if they don't pay.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = SpellCastLifeDrainEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleOpponentSpellCastDrain(TriggerMatchContext match,
            SpellCastLifeDrainEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // "That player loses N life and you gain M life" — carry the casting opponent on targetId so the
        // TARGET_PLAYER life loss lands on them (the casting player is not a chosen target), then the
        // controller gains the fixed amount. Omit the gain step when the card only drains life.
        List<CardEffect> drainEffects = new ArrayList<>();
        drainEffects.add(new LoseLifeEffect(trigger.lifeLoss(), LoseLifeRecipient.TARGET_PLAYER));
        if (trigger.lifeGain() > 0) {
            drainEffects.add(new GainLifeEffect(trigger.lifeGain()));
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                drainEffects
        );
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = SpellCastDamageToCasterEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    @CollectsTrigger(value = SpellCastDamageToCasterEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleSpellCastDamage(TriggerMatchContext match,
            SpellCastDamageToCasterEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.onlyWhenCasterNotActiveTurn()
                && match.gameData().activePlayerId != null
                && match.gameData().activePlayerId.equals(sc.castingPlayerId())) {
            return false;
        }
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // Intervening "if" (CR 603.4): the condition is checked against the source permanent as the
        // spell is cast — the ability doesn't trigger at all when it fails — and again on resolution,
        // which the ConditionalEffect wrapper below does.
        if (trigger.intervening() != null && !conditionEvaluationService.isMet(match.gameData(),
                trigger.intervening(), ConditionContext.forPermanent(match.permanent(), match.controllerId()))) {
            return false;
        }
        CardEffect damageEffect = new DealDamageToPlayersEffect(trigger.damage(), DamageRecipient.TARGET_PLAYER);
        if (trigger.intervening() != null) {
            damageEffect = new ConditionalEffect(trigger.intervening(), damageEffect);
        }
        // "This creature deals N damage to that player" — carry the casting opponent on targetId so the
        // TARGET_PLAYER damage lands on them (the casting player is not a chosen target), and the source
        // permanent id so the damage is attributed to this permanent.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(damageEffect)),
                null,
                match.permanent().getId()
        );
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleOpponentColorCounter(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!trigger.matchesColor(sc.spellCard().getColor())) return false;
        return addColorCounterTrigger(match, trigger);
    }

    // ── Shared helpers ─────────────────────────────────────────────────

    @CollectsTrigger(value = SpellCopyTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_COPIES_SPELL)
    @CollectsTrigger(value = SpellCopyTriggerEffect.class, slot = EffectSlot.ON_OPPONENT_COPIES_SPELL)
    private boolean handleSpellCopyTrigger(TriggerMatchContext match,
            SpellCopyTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCopy spellCopy = (TriggerContext.SpellCopy) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(
                spellCopy.copiedSpell().getCard(), trigger.spellFilter(),
                match.permanent().getOriginalCard().getId(), match.gameData(),
                spellCopy.copyingPlayerId())) {
            return false;
        }

        List<CardEffect> resolved = new ArrayList<>(trigger.resolvedEffects());
        boolean needsPlayerTarget = resolved.stream()
                .anyMatch(effect -> triggerTargetSpec(effect).admits(TargetPredicate.Kind.PLAYER));
        boolean needsPermanentTarget = resolved.stream()
                .anyMatch(effect -> triggerTargetSpec(effect).admits(TargetPredicate.Kind.PERMANENT));
        if (needsPlayerTarget || needsPermanentTarget) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), resolved,
                    needsPlayerTarget && !needsPermanentTarget, trigger.targetFilter(), 0,
                    match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers — choose a target."));
            return true;
        }

        boolean needsGraveyardTarget = resolved.stream()
                .anyMatch(effect -> triggerTargetSpec(effect).admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        if (needsGraveyardTarget) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), resolved));
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                resolved,
                spellCopy.copyingPlayerId(),
                match.permanent().getId());
        entry.setTriggeringCardId(spellCopy.copiedSpell().getCard().getId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    private boolean handleGenericSpellCastTrigger(TriggerMatchContext match, SpellCastTriggerEffect trigger,
                                                    Card spellCard, UUID castingPlayerId) {
        // "Whenever you cast a spell during an opponent's turn" — the source's controller must not be
        // the active player when the spell is cast (Glen Elendra Pranksters).
        if (trigger.onlyDuringOpponentTurn()
                && match.controllerId().equals(match.gameData().activePlayerId)) return false;

        // "Whenever an opponent casts a spell during your turn" — the source's controller must be
        // the active player when the spell is cast (Eyes of the Wisent).
        if (trigger.onlyDuringControllerTurn()
                && !match.controllerId().equals(match.gameData().activePlayerId)) return false;

        if (!predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(),
                match.permanent().getOriginalCard().getId(),
                match.gameData(), castingPlayerId)) return false;

        if (trigger.nthSpellNumber() > 0 && !isNthMatchingSpell(match.gameData(), trigger, castingPlayerId)) {
            return false;
        }

        if (trigger.minimumSpellNumber() > 0
                && !hasAtLeastMatchingSpell(match.gameData(), trigger, castingPlayerId)) {
            return false;
        }

        if (trigger.intervening() != null
                && !conditionEvaluationService.isMet(match.gameData(), trigger.intervening(),
                ConditionContext.forPermanent(match.permanent(), match.controllerId())
                        .withXValue(match.gameData().getSpellCastManaSpent(spellCard.getId())))) {
            return false;
        }

        // Repartee-style condition on the cast spell's chosen targets (e.g. "targets a creature"),
        // or source-relative mana-value gates (e.g. Imminent Doom — MV equals counters on source).
        if (trigger.castSpellTargetCondition() != null) {
            StackEntry spellEntry = findStackEntryForCard(match.gameData(), spellCard.getId());
            if (spellEntry == null) return false;
            // Evaluated from the trigger source's controller, so "you"/"you control" in the predicate
            // means the ability's controller — not the caster (Reparations, an opponent-cast trigger).
            if (!targetLegalityService.matchesStackEntryPredicate(match.gameData(), spellEntry,
                    trigger.castSpellTargetCondition(), match.controllerId(), match.permanent())) return false;
        }

        List<CardEffect> resolved = new ArrayList<>(trigger.resolvedEffects());
        StackEntry triggeringSpell = findStackEntryForCard(match.gameData(), spellCard.getId());
        if (triggeringSpell != null) {
            StackEntry spellSnapshot = new StackEntry(triggeringSpell);
            resolved = resolved.stream()
                    .map(effect -> snapshotTriggeringSpell(effect, spellSnapshot, castingPlayerId))
                    .toList();
        }
        // Snapshot CountersOnSource damage at trigger time (Imminent Doom ruling: damage equals
        // the counter count as the ability triggered, not as it resolves).
        resolved = snapshotCountersOnSourceDamage(resolved, match.permanent());
        if (trigger.intervening() != null) {
            resolved = resolved.stream()
                    .map(effect -> (CardEffect) new ConditionalEffect(trigger.intervening(), effect))
                    .toList();
        }
        boolean selfTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).selfTargeting());
        boolean attachedSelfTarget = resolved.stream().anyMatch(
                e -> e instanceof AttachedPermanentSelfTargetingEffect && e.targetSpec().selfTargeting());
        boolean needsPlayerTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.PLAYER));
        boolean needsPermanentTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.PERMANENT));
        boolean needsGraveyardTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        boolean needsSpellTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.SPELL));
        boolean needsTargeting = needsPlayerTarget || needsPermanentTarget;
        boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;
        boolean countersTriggeringSpell = resolved.stream().anyMatch(CounterSpellingEffect.class::isInstance);
        boolean needsSpellManaSpentX = resolved.stream().anyMatch(this::effectNeedsSpellManaSpentX);
        int spellManaSpentX = needsSpellManaSpentX
                ? match.gameData().getSpellCastManaSpent(spellCard.getId()) : 0;
        boolean carriesTriggeringSpellManaValue = resolved.stream()
                .anyMatch(CreateTokenForTriggeringPlayerEffect.class::isInstance);
        int triggeringSpellManaValue = carriesTriggeringSpellManaValue
                ? spellManaValue(match.gameData(), spellCard) : 0;

        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(PendingMayAbility.forSpellCastTrigger(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolved,
                    match.permanent().getCard().getName() + " — " + may.prompt(),
                    trigger.manaCost(),
                    match.permanent().getId(),
                    spellCard.getId()));
        } else if (resolved.size() == 1 && resolved.getFirst() instanceof ChooseOneEffect chooseOneEffect) {
            match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    match.permanent().getCard(), match.controllerId(), chooseOneEffect, match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        } else if (needsGraveyardTarget) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), resolved
            ));
            log.info("Game {} - {} spell-cast graveyard-target trigger queued",
                    match.gameData().id, match.permanent().getCard().getName());
        } else if (needsSpellTarget && !needsTargeting) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    resolved,
                    0,
                    spellCard.getId(),
                    match.permanent().getId(),
                    Map.of(),
                    Zone.STACK,
                    List.of(),
                    List.of()
            );
            entry.setTriggeringCardId(spellCard.getId());
            match.gameData().stack.add(entry);
        } else if (needsTargeting) {
            // "You may pay {C}. If you do, [targeted effect]" (Malachite Talisman): the target is chosen
            // as the trigger goes on the stack (CR 603.3d), while the payment choice waits for resolution
            // (CR 603.5) —
            // so wrap after the targeting decision has been made from the unwrapped effects.
            List<CardEffect> queued = trigger.manaCost() == null
                    ? resolved
                    : resolved.stream().map(e -> (CardEffect) new MayPayManaEffect(
                            trigger.manaCost(), e, "Pay " + trigger.manaCost() + "?")).toList();
            Card sourceCard = match.permanent().getCard();
            boolean multiTarget = sourceCard.getSpellTargets().size() > 1
                    || sourceCard.getSpellTargets().stream()
                    .anyMatch(target -> target.getMaxTargets() > 1 || target.getMinTargets() == 0);
            if (multiTarget) {
                match.gameData().queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        sourceCard, match.controllerId(), queued, match.permanent().getId(),
                        List.of(), 0, 0));
            } else {
                match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        sourceCard, match.controllerId(), queued, playerTargetOnly, trigger.targetFilter(),
                        spellManaSpentX, match.permanent().getId()
                ));
            }
            gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers — choose a target."));
        } else {
            StackEntry entry;
            if (countersTriggeringSpell) {
                entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        match.permanent().getCard(),
                        match.controllerId(),
                        match.permanent().getCard().getName() + "'s ability",
                        resolved,
                        spellManaSpentX,
                        spellCard.getId(),
                        match.permanent().getId(),
                        null,
                        Zone.STACK,
                        null,
                        null
                );
            } else if (selfTarget) {
                UUID selfTargetId = attachedSelfTarget
                        ? match.permanent().getAttachedTo()
                        : castingPlayerId;
                entry = spellManaSpentX > 0
                        ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                            match.permanent().getCard().getName() + "'s ability", resolved, spellManaSpentX,
                            selfTargetId, match.permanent().getId(), null, null, null, null)
                        : new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                            match.permanent().getCard().getName() + "'s ability", resolved, selfTargetId,
                            match.permanent().getId());
                entry.setNonTargeting(true);
            } else {
                entry = spellManaSpentX > 0
                        ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                            match.permanent().getCard().getName() + "'s ability", resolved, spellManaSpentX,
                            null, match.permanent().getId(), null, null, null, null)
                        : new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                            match.permanent().getCard().getName() + "'s ability", resolved, null,
                            match.permanent().getId());
                // Contextual "that player" = the caster (Leshrac's Sigil may-pay look-at-hand).
                // Non-targeting: oracle does not use the word "target".
                entry.setTargetId(castingPlayerId);
                entry.setNonTargeting(true);
            }
            // Contextual "it" = the spell that caused the trigger, still on the stack below this
            // ability (Bloodlord of Vaasgoth's bloodthirst grant).
            entry.setTriggeringCardId(spellCard.getId());
            if (carriesTriggeringSpellManaValue) {
                entry.setEventValue(triggeringSpellManaValue);
            }
            match.gameData().stack.add(entry);
        }
        return true;
    }

    private boolean isNthMatchingSpell(GameData gameData, SpellCastTriggerEffect trigger, UUID playerId) {
        return matchingSpellCount(gameData, trigger, playerId) == trigger.nthSpellNumber();
    }

    private boolean hasAtLeastMatchingSpell(GameData gameData, SpellCastTriggerEffect trigger, UUID playerId) {
        return matchingSpellCount(gameData, trigger, playerId) >= trigger.minimumSpellNumber();
    }

    private long matchingSpellCount(GameData gameData, SpellCastTriggerEffect trigger, UUID playerId) {
        long matchingSpells = gameData.getSpellsCastThisTurn(playerId).stream()
                .filter(spell -> predicateEvaluationService.matchesCardPredicate(
                        spell, trigger.spellFilter(), null, gameData, playerId))
                .count();
        return matchingSpells;
    }

    private boolean hasOptionalSingleTarget(Card card, CardEffect effect) {
        if (card.getSpellTargets().size() != 1) {
            return false;
        }
        var target = card.getSpellTargets().getFirst();
        return target.getMinTargets() == 0
                && target.getMaxTargets() == 1
                && card.getEffectTargetIndex(effect) == target.getIndex();
    }

    /**
     * The targeting a spell-cast trigger must resolve before it goes on the stack.
     * {@link MayPayManaEffect} delegates its spec to the effect it wraps, which is right for
     * "you may pay {X}. If you do, [targeted effect]" abilities whose cost lives on the trigger
     * ({@code trigger.manaCost()}). A card that instead writes the payment into its own resolved
     * effects settles everything at resolution, and its player is the caster bound below as a
     * non-target (Leshrac's Sigil's "that player's hand"), so it must not open a target choice.
     */
    private static TargetSpec triggerTargetSpec(CardEffect effect) {
        return effect instanceof MayPayManaEffect ? TargetSpec.NONE : effect.targetSpec();
    }

    private List<CardEffect> snapshotCountersOnSourceDamage(List<CardEffect> effects, Permanent source) {
        List<CardEffect> snapshotted = new ArrayList<>(effects.size());
        for (CardEffect effect : effects) {
            if (effect instanceof DealDamageToAnyTargetEffect damage
                    && damage.damage() instanceof CountersOnSource counters) {
                int count = source.getCounterCount(counters.counterType());
                snapshotted.add(new DealDamageToAnyTargetEffect(
                        new Fixed(count), damage.cantRegenerate(), damage.exileInsteadOfDie()));
            } else {
                snapshotted.add(effect);
            }
        }
        return snapshotted;
    }

    private CardEffect snapshotTriggeringSpell(CardEffect effect, StackEntry spellSnapshot,
                                               UUID castingPlayerId) {
        if (effect instanceof FlipCoinCopyTriggeringSpellOrDealDamageEffect breechesEffect
                && breechesEffect.spellSnapshot() == null) {
            return new FlipCoinCopyTriggeringSpellOrDealDamageEffect(
                    new StackEntry(spellSnapshot), castingPlayerId);
        }
        if (effect instanceof MayEffect may) {
            CardEffect wrapped = snapshotTriggeringSpell(may.wrapped(), spellSnapshot, castingPlayerId);
            CardEffect elseEffect = may.elseEffect() == null
                    ? null
                    : snapshotTriggeringSpell(may.elseEffect(), spellSnapshot, castingPlayerId);
            return new MayEffect(wrapped, may.prompt(), elseEffect, may.choicePlayer());
        }
        if (effect instanceof SacrificePermanentThenEffect sacrifice && sacrifice.thenEffect() != null) {
            return new SacrificePermanentThenEffect(
                    sacrifice.filter(),
                    snapshotTriggeringSpell(sacrifice.thenEffect(), spellSnapshot, castingPlayerId),
                    sacrifice.permanentDescription(),
                    sacrifice.targetBeforeSacrifice(),
                    sacrifice.reflexive());
        }
        return effect;
    }

    private StackEntry findStackEntryForCard(com.github.laxika.magicalvibes.model.GameData gameData, UUID cardId) {
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(cardId)) {
                return se;
            }
        }
        return null;
    }

    private int spellManaValue(com.github.laxika.magicalvibes.model.GameData gameData, Card spellCard) {
        StackEntry spellEntry = findStackEntryForCard(gameData, spellCard.getId());
        return spellCard.getManaValue() + (spellEntry == null ? 0 : spellEntry.getXValue());
    }

    private boolean effectNeedsSpellManaSpentX(CardEffect effect) {
        if (effect instanceof BoostSelfEffect boost
                && (amountEvaluationService.referencesXValue(boost.powerBoost())
                || amountEvaluationService.referencesXValue(boost.toughnessBoost()))) {
            return true;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return conditional.condition() instanceof SpellManaSpentAtLeast
                    || conditional.condition() instanceof SpellManaSpentGreaterThanSourcePower
                    || effectNeedsSpellManaSpentX(conditional.wrapped());
        }
        if (effect instanceof ConditionalReplacementEffect replacement) {
            return replacement.condition() instanceof SpellManaSpentAtLeast
                    || replacement.condition() instanceof SpellManaSpentGreaterThanSourcePower
                    || effectNeedsSpellManaSpentX(replacement.baseEffect())
                    || effectNeedsSpellManaSpentX(replacement.upgradedEffect());
        }
        return false;
    }

    private boolean addColorCounterTrigger(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger) {
        return addColorCounterTrigger(match, trigger.amount());
    }

    private boolean addColorCounterTrigger(TriggerMatchContext match, int amount) {
        List<CardEffect> resolvedEffects = List.of(new PutCountersOnSourceEffect(1, 1, amount));

        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolvedEffects,
                    match.permanent().getCard().getName() + " — " + may.prompt(),
                    null,
                    null,
                    match.permanent().getId()
            ));
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(resolvedEffects),
                    null,
                    match.permanent().getId()
            ));
        }
        return true;
    }
}
