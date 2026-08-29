package com.github.laxika.magicalvibes.service.effect.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.RemoveCountersFromControlledCreaturesCastingCost;
import com.github.laxika.magicalvibes.model.RemoveXCountersFromControlledPermanentsCastingCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.BlightCost;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeCost;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellAdditionalCountersCostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayLifeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrSacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.EscalateDiscardCost;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.EscalateSacrificeCost;
import com.github.laxika.magicalvibes.model.effect.EscalateTapCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeOrSacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnControlledCreatureOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReturnAnyNumberOfPermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllPermanentsYouControlCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardCost;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.effect.TieredManaCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The single home for non-mana <b>additional cast costs</b> (CR 601.2b/601.2h): which cost
 * types exist on a spell, whether they are satisfiable at all, and whether a concrete payment
 * selection is legal. Every cast path must call {@link #validateAll} (or the per-cost validators)
 * <b>before any cost is paid</b> — a cast either completes or leaves the game state untouched;
 * a throw after partial payment would leak the mana/costs already consumed, because the engine
 * has no transactional rewind (payments broadcast log entries eagerly).
 *
 * <p>This service is deliberately <b>pure</b> — it mutates nothing and depends on nothing that
 * broadcasts. Payment (mutation + logging + triggers) stays in {@code SpellCastingService}, whose
 * pay methods each start by calling their validator here. {@code CostEffectClassificationTest}
 * fails the build when a new {@code CostEffect} type is not classified in
 * {@link #HANDLED_SPELL_COST_TYPES} (or its ability-only list), so a new cost type cannot be
 * silently invisible to satisfiability/validation the way {@code DiscardCardTypeCost} once was.
 */
@Component
@RequiredArgsConstructor
public class AdditionalSpellCostService {

    /**
     * Every additional-cast-cost type this service knows how to check. A spell-slot
     * {@code CostEffect} not in this set is invisible to satisfiability and validation —
     * the classification guard test enforces that this never happens silently.
     */
    public static final Set<Class<? extends CardEffect>> HANDLED_SPELL_COST_TYPES = Set.of(
            SacrificeAllCreaturesYouControlCost.class,
            SacrificeAllPermanentsYouControlCost.class,
            SacrificeCreatureCost.class,
            SacrificeCreatureOrPayManaCost.class,
            SacrificePermanentOrPayManaCost.class,
            SacrificePermanentOrDiscardCardCost.class,
            SacrificePermanentCost.class,
            ExileCreatureCost.class,
            SacrificeMultiplePermanentsCost.class,
            EscalateSacrificeCost.class,
            EscalateTapCost.class,
            SacrificeAnyNumberOfPermanentsCost.class,
            TapAnyNumberOfPermanentsCost.class,
            TapMultiplePermanentsCost.class,
            ReturnAnyNumberOfPermanentsToHandCost.class,
            ReturnPermanentToHandCost.class,
            ReturnCreatureToHandCost.class,
            BlightCost.class,
            PutCounterOnControlledCreatureCost.class,
            PutCountersOnControlledCreatureOrPayManaCost.class,
            PayXLifeCost.class,
            PayLifeCost.class,
            PayLifeOrPayManaCost.class,
            PayLifeOrSacrificePermanentCost.class,
            ExileCardFromGraveyardCost.class,
            ExileXCardsFromGraveyardCost.class,
            CollectEvidenceCost.class,
            ExileNCardsFromGraveyardCost.class,
            DiscardCardTypeCost.class,
            DiscardRandomCardCost.class,
            DiscardCardOrPayManaCost.class,
            DiscardCardOrPayLifeCost.class,
            DiscardCardOrSacrificePermanentCost.class,
            DiscardHandCost.class,
            DiscardXCardsCost.class,
            EscalateDiscardCost.class,
            EscalateManaCost.class,
            RepeatableAdditionalManaCost.class,
            ChooseXValueCost.class,
            ChooseCreatureTypeCost.class,
            BeholdAndExileCost.class,
            BeholdCost.class,
            DelveCost.class,
            RevealCardFromHandCost.class,
            TieredManaCost.class,
            SpreeAdditionalManaCost.class,
            WaterbendCost.class);

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * The additional cast costs found on one spell, in canonical payment order. Extracted once
     * per cast so extraction, validation and payment can never disagree about what the spell
     * costs. {@code SpellCastingService.payAdditionalCosts} must consume every field.
     */
    public record ExtractedCosts(
            boolean sacrificeAllCreatures,
            boolean sacrificeAllPermanents,
            boolean sacrificeCreature,
            SacrificePermanentOrPayManaCost sacrificePermanentOrPayManaCost,
            SacrificePermanentOrDiscardCardCost sacrificePermanentOrDiscardCardCost,
            SacrificePermanentCost sacrificePermanentCost,
            DiscardCardOrSacrificePermanentCost discardCardOrSacrificePermanentCost,
            ExileCreatureCost exileCreatureCost,
            SacrificeMultiplePermanentsCost sacrificeMultiplePermanentsCost,
            EscalateSacrificeCost escalateSacrificeCost,
            EscalateTapCost escalateTapCost,
            SacrificeAnyNumberOfPermanentsCost sacrificeAnyNumberCost,
            TapAnyNumberOfPermanentsCost tapAnyNumberCost,
            TapMultiplePermanentsCost tapMultipleCost,
            ReturnAnyNumberOfPermanentsToHandCost returnAnyNumberCost,
            ReturnPermanentToHandCost returnPermanentToHand,
            boolean returnCreatureToHand,
            BlightCost blightCost,
            PutCounterOnControlledCreatureCost putCounterCost,
            PutCountersOnControlledCreatureOrPayManaCost putCountersOrPayManaCost,
            boolean payXLife,
            PayLifeCost payLifeCost,
            PayLifeOrPayManaCost payLifeOrPayManaCost,
            ExileCardFromGraveyardCost exileGraveyardCost,
            ExileXCardsFromGraveyardCost exileXCardsCost,
            CollectEvidenceCost collectEvidenceCost,
            ExileNCardsFromGraveyardCost exileNCardsCost,
            DiscardCardTypeCost discardCost,
            DiscardRandomCardCost discardRandomCost,
            DiscardCardOrPayManaCost discardCardOrPayManaCost,
            DiscardCardOrPayLifeCost discardCardOrPayLifeCost,
            boolean discardHand,
            DiscardXCardsCost discardXCardsCost,
            EscalateDiscardCost escalateDiscardCost,
            EscalateManaCost escalateManaCost,
            RepeatableAdditionalManaCost repeatableManaCost,
            ChooseXValueCost chooseXValueCost,
            BeholdAndExileCost beholdCost,
            BeholdCost beholdSelectionCost,
            DelveCost delveCost,
            RevealCardFromHandCost revealCardCost,
            ChooseCreatureTypeCost chooseCreatureTypeCost,
            TieredManaCost tieredManaCost,
            PayLifeOrSacrificePermanentCost payLifeOrSacrificePermanentCost,
            SpreeAdditionalManaCost spreeAdditionalManaCost,
            WaterbendCost waterbendCost
    ) {
        /** True when the spell has any additional cast cost at all. */
        public boolean any() {
            return hasNonEscalateCost() || hasEscalate();
        }

        /** True when the spell has an additional cost that is not charged per extra mode. */
        public boolean hasNonEscalateCost() {
            return sacrificeAllCreatures || sacrificeAllPermanents || sacrificeCreature
                    || sacrificePermanentCost != null || exileCreatureCost != null
                    || sacrificeMultiplePermanentsCost != null
                    || sacrificePermanentOrPayManaCost != null
                    || sacrificePermanentOrDiscardCardCost != null
                    || sacrificeAnyNumberCost != null
                    || tapAnyNumberCost != null || tapMultipleCost != null || returnAnyNumberCost != null
                    || returnPermanentToHand != null
                    || returnCreatureToHand || blightCost != null || putCounterCost != null || putCountersOrPayManaCost != null
                    || payXLife || payLifeCost != null || payLifeOrPayManaCost != null
                    || payLifeOrSacrificePermanentCost != null
                    || discardCardOrSacrificePermanentCost != null
                    || exileGraveyardCost != null || exileXCardsCost != null
                    || collectEvidenceCost != null || exileNCardsCost != null
                    || discardCost != null || discardRandomCost != null || discardCardOrPayManaCost != null
                    || discardCardOrPayLifeCost != null
                    || discardHand || discardXCardsCost != null
                    || repeatableManaCost != null || chooseXValueCost != null
                    || beholdCost != null || beholdSelectionCost != null || delveCost != null
                    || revealCardCost != null || chooseCreatureTypeCost != null
                    || tieredManaCost != null
                    || spreeAdditionalManaCost != null || waterbendCost != null;
        }

        /** True when the spell has any per-extra-mode cost. */
        public boolean hasEscalate() {
            return escalateDiscardCost != null || escalateManaCost != null || escalateSacrificeCost != null
                    || escalateTapCost != null;
        }
    }

    /**
     * The caster's payment choices, as carried by the cast request. {@code spellCardIndex} is the
     * spell's own pre-removal hand index (used to adjust {@code discardHandCardIndex}); pass a
     * negative value for casts not from hand.
     * <p>
     * {@code discardHandCardIndices} pays escalate (one discard per mode beyond the first);
     * {@code escalateModeCount} is the number of modes chosen for that escalate payment (0 when
     * unused). {@code sacrificePermanentIds} pays any multi-permanent cost — a multi-permanent
     * sacrifice (Phyrexian Tribute's "sacrifice two creatures" or an entwine sacrifice), a "tap any number of permanents
     * you control" cost (Burn at the Stake), or a "return any number of permanents you control to
     * hand" cost (Infernal Harvest); no spell carries more than one of these. The single-permanent
     * costs keep using {@code sacrificePermanentId}.
     */
    public record CostSelection(
            UUID sacrificePermanentId,
            Integer exileGraveyardCardIndex,
            List<Integer> exileGraveyardCardIndices,
            Integer discardHandCardIndex,
            List<Integer> discardHandCardIndices,
            int escalateModeCount,
            int spellCardIndex,
            List<UUID> sacrificePermanentIds,
            UUID beholdPermanentId,
            Integer beholdHandCardIndex,
            List<UUID> beholdPermanentIds,
            List<Integer> beholdHandCardIndices,
            CardSubtype beholdChosenSubtype,
            Boolean payLifeForAdditionalCost
    ) {
        public CostSelection(UUID sacrificePermanentId, Integer exileGraveyardCardIndex,
                             List<Integer> exileGraveyardCardIndices, Integer discardHandCardIndex,
                             List<Integer> discardHandCardIndices, int escalateModeCount, int spellCardIndex,
                             List<UUID> sacrificePermanentIds, UUID beholdPermanentId,
                             Integer beholdHandCardIndex, List<UUID> beholdPermanentIds,
                             List<Integer> beholdHandCardIndices) {
            this(sacrificePermanentId, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    discardHandCardIndex, discardHandCardIndices, escalateModeCount, spellCardIndex,
                    sacrificePermanentIds, beholdPermanentId, beholdHandCardIndex, beholdPermanentIds,
                    beholdHandCardIndices, null, null);
        }

        public CostSelection(UUID sacrificePermanentId, Integer exileGraveyardCardIndex,
                             List<Integer> exileGraveyardCardIndices, Integer discardHandCardIndex,
                             List<Integer> discardHandCardIndices, int escalateModeCount, int spellCardIndex,
                             List<UUID> sacrificePermanentIds, UUID beholdPermanentId,
                             Integer beholdHandCardIndex) {
            this(sacrificePermanentId, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    discardHandCardIndex, discardHandCardIndices, escalateModeCount, spellCardIndex,
                    sacrificePermanentIds, beholdPermanentId, beholdHandCardIndex, List.of(), List.of());
        }

        public static CostSelection none() {
            return new CostSelection(null, null, null, null, null, 0, -1, List.of(), null, null,
                    List.of(), List.of());
        }

        /** Convenience for the common single-discard / no-escalate case. */
        public CostSelection(UUID sacrificePermanentId, Integer exileGraveyardCardIndex,
                             List<Integer> exileGraveyardCardIndices, Integer discardHandCardIndex,
                             int spellCardIndex) {
            this(sacrificePermanentId, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    discardHandCardIndex, null, 0, spellCardIndex, List.of(), null, null,
                    List.of(), List.of());
        }

        public CostSelection(UUID sacrificePermanentId, Integer exileGraveyardCardIndex,
                             List<Integer> exileGraveyardCardIndices, Integer discardHandCardIndex,
                             List<Integer> discardHandCardIndices, int escalateModeCount, int spellCardIndex) {
            this(sacrificePermanentId, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    discardHandCardIndex, discardHandCardIndices, escalateModeCount, spellCardIndex,
                    List.of(), null, null, List.of(), List.of());
        }

        public CostSelection(UUID sacrificePermanentId, Integer exileGraveyardCardIndex,
                             List<Integer> exileGraveyardCardIndices, Integer discardHandCardIndex,
                             List<Integer> discardHandCardIndices, int escalateModeCount, int spellCardIndex,
                             List<UUID> sacrificePermanentIds) {
            this(sacrificePermanentId, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    discardHandCardIndex, discardHandCardIndices, escalateModeCount, spellCardIndex,
                    sacrificePermanentIds, null, null, List.of(), List.of());
        }
    }

    /**
     * Removes every handled additional-cost effect from {@code effects} (a mutable copy of the
     * spell's SPELL-slot effects — never the frozen card list itself) and returns them. The
     * stripped list is what goes onto the stack; costs are paid at cast time, not resolved.
     */
    public ExtractedCosts extractAndRemove(List<CardEffect> effects) {
        boolean sacAllCreatures = effects.removeIf(SacrificeAllCreaturesYouControlCost.class::isInstance);
        boolean sacAllPermanents = effects.removeIf(SacrificeAllPermanentsYouControlCost.class::isInstance);
        boolean sacCreature = effects.removeIf(SacrificeCreatureCost.class::isInstance);
        SacrificeCreatureOrPayManaCost legacySacOrPay =
                removeFirst(effects, SacrificeCreatureOrPayManaCost.class);
        SacrificePermanentOrPayManaCost sacOrPay = removeFirst(effects, SacrificePermanentOrPayManaCost.class);
        if (sacOrPay == null && legacySacOrPay != null) {
            sacOrPay = new SacrificePermanentOrPayManaCost(
                    legacySacOrPay.manaCost(), new PermanentIsCreaturePredicate(), "a creature");
        }
        SacrificePermanentOrDiscardCardCost sacOrDiscard =
                removeFirst(effects, SacrificePermanentOrDiscardCardCost.class);
        SacrificePermanentCost permCost = removeFirst(effects, SacrificePermanentCost.class);
        DiscardCardOrSacrificePermanentCost discardOrSacrifice =
                removeFirst(effects, DiscardCardOrSacrificePermanentCost.class);
        ExileCreatureCost exileCreatureCost = removeFirst(effects, ExileCreatureCost.class);
        SacrificeMultiplePermanentsCost multiPermCost = removeFirst(effects, SacrificeMultiplePermanentsCost.class);
        EscalateSacrificeCost escalateSacrificeCost = removeFirst(effects, EscalateSacrificeCost.class);
        EscalateTapCost escalateTapCost = removeFirst(effects, EscalateTapCost.class);
        SacrificeAnyNumberOfPermanentsCost sacAnyNumberCost =
                removeFirst(effects, SacrificeAnyNumberOfPermanentsCost.class);
        TapAnyNumberOfPermanentsCost tapAnyNumberCost = removeFirst(effects, TapAnyNumberOfPermanentsCost.class);
        TapMultiplePermanentsCost tapMultipleCost = removeFirst(effects, TapMultiplePermanentsCost.class);
        ReturnAnyNumberOfPermanentsToHandCost returnAnyNumberCost =
                removeFirst(effects, ReturnAnyNumberOfPermanentsToHandCost.class);
        ReturnPermanentToHandCost returnPermanentToHand = removeFirst(effects, ReturnPermanentToHandCost.class);
        boolean returnCreature = effects.removeIf(ReturnCreatureToHandCost.class::isInstance);
        BlightCost blightCost = removeFirst(effects, BlightCost.class);
        PutCounterOnControlledCreatureCost putCounterCost = removeFirst(effects, PutCounterOnControlledCreatureCost.class);
        PutCountersOnControlledCreatureOrPayManaCost putCountersOrPayManaCost =
                removeFirst(effects, PutCountersOnControlledCreatureOrPayManaCost.class);
        boolean payXLife = effects.removeIf(PayXLifeCost.class::isInstance);
        PayLifeCost payLifeCost = removeFirst(effects, PayLifeCost.class);
        PayLifeOrPayManaCost payLifeOrPayManaCost = removeFirst(effects, PayLifeOrPayManaCost.class);
        PayLifeOrSacrificePermanentCost payLifeOrSacrificePermanentCost =
                removeFirst(effects, PayLifeOrSacrificePermanentCost.class);
        ExileCardFromGraveyardCost exileGraveyardCost = removeFirst(effects, ExileCardFromGraveyardCost.class);
        ExileXCardsFromGraveyardCost exileXCardsCost = removeFirst(effects, ExileXCardsFromGraveyardCost.class);
        CollectEvidenceCost collectEvidenceCost = removeFirst(effects, CollectEvidenceCost.class);
        ExileNCardsFromGraveyardCost exileNCardsCost = removeFirst(effects, ExileNCardsFromGraveyardCost.class);
        DiscardCardTypeCost discardCost = removeFirst(effects, DiscardCardTypeCost.class);
        DiscardRandomCardCost discardRandomCost = removeFirst(effects, DiscardRandomCardCost.class);
        DiscardCardOrPayManaCost discardOrPay = removeFirst(effects, DiscardCardOrPayManaCost.class);
        DiscardCardOrPayLifeCost discardOrPayLife = removeFirst(effects, DiscardCardOrPayLifeCost.class);
        boolean discardHand = effects.removeIf(DiscardHandCost.class::isInstance);
        DiscardXCardsCost discardXCards = removeFirst(effects, DiscardXCardsCost.class);
        EscalateDiscardCost escalateDiscardCost = removeFirst(effects, EscalateDiscardCost.class);
        EscalateManaCost escalateManaCost = removeFirst(effects, EscalateManaCost.class);
        List<RepeatableAdditionalManaCost> repeatableManaCosts = effects.stream()
                .filter(RepeatableAdditionalManaCost.class::isInstance)
                .map(RepeatableAdditionalManaCost.class::cast)
                .toList();
        effects.removeIf(RepeatableAdditionalManaCost.class::isInstance);
        RepeatableAdditionalManaCost repeatableManaCost = repeatableManaCosts.isEmpty()
                ? null
                : repeatableManaCosts.size() == 1
                ? repeatableManaCosts.getFirst()
                : RepeatableAdditionalManaCost.combine(repeatableManaCosts);
        ChooseXValueCost chooseXValueCost = removeFirst(effects, ChooseXValueCost.class);
        BeholdAndExileCost beholdCost = removeFirst(effects, BeholdAndExileCost.class);
        BeholdCost beholdSelectionCost = removeFirst(effects, BeholdCost.class);
        DelveCost delveCost = removeFirst(effects, DelveCost.class);
        RevealCardFromHandCost revealCardCost = removeFirst(effects, RevealCardFromHandCost.class);
        ChooseCreatureTypeCost chooseCreatureTypeCost = removeFirst(effects, ChooseCreatureTypeCost.class);
        TieredManaCost tieredManaCost = removeFirst(effects, TieredManaCost.class);
        SpreeAdditionalManaCost spreeAdditionalManaCost = removeFirst(effects, SpreeAdditionalManaCost.class);
        WaterbendCost waterbendCost = removeFirst(effects, WaterbendCost.class);
        return new ExtractedCosts(sacAllCreatures, sacAllPermanents, sacCreature, sacOrPay,
                sacOrDiscard, permCost, discardOrSacrifice, exileCreatureCost, multiPermCost,
                escalateSacrificeCost, escalateTapCost,
                sacAnyNumberCost, tapAnyNumberCost, tapMultipleCost, returnAnyNumberCost,
                returnPermanentToHand, returnCreature,
                blightCost, putCounterCost, putCountersOrPayManaCost,
                payXLife, payLifeCost, payLifeOrPayManaCost, exileGraveyardCost, exileXCardsCost,
                collectEvidenceCost, exileNCardsCost, discardCost, discardRandomCost,
                discardOrPay, discardOrPayLife,
                discardHand, discardXCards, escalateDiscardCost, escalateManaCost, repeatableManaCost,
                chooseXValueCost, beholdCost, beholdSelectionCost, delveCost, revealCardCost,
                chooseCreatureTypeCost, tieredManaCost,
                payLifeOrSacrificePermanentCost, spreeAdditionalManaCost, waterbendCost);
    }

    /** Adds additional costs granted by permanents before extracting the spell's cast costs. */
    public ExtractedCosts extractAndRemove(GameData gameData, UUID playerId, Card card,
                                           List<CardEffect> effects) {
        if (effects.stream().noneMatch(DelveCost.class::isInstance)
                && gameQueryService.hasSpellCastingAbilityGrant(gameData, playerId, card, Keyword.DELVE)) {
            effects.add(new DelveCost());
        }
        if (hasCreatureSpellAdditionalCountersCost(gameData, playerId, card)) {
            effects.add(new RepeatableAdditionalManaCost(List.of("{1}")));
        }
        if (card.getManaCost() != null
                && gameQueryService.hasSpellCastingAbilityGrant(gameData, playerId, card, Keyword.REPLICATE)) {
            effects.add(new RepeatableAdditionalManaCost(List.of(card.getManaCost())));
        }
        return extractAndRemove(effects);
    }

    /** Returns whether an active Chorus of the Conclave-style effect applies to this spell. */
    public boolean hasCreatureSpellAdditionalCountersCost(GameData gameData, UUID playerId, Card card) {
        if (!gameQueryService.cardHasType(card, CardType.CREATURE, gameData, playerId)) {
            return false;
        }
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .anyMatch(permanent -> gameQueryService.hasActiveStaticEffect(
                        gameData, permanent, CreatureSpellAdditionalCountersCostEffect.class));
    }

    /**
     * Resolves the X value supplied by an additional cost whose payment count defines X.
     * This is needed before cast-time target validation, while payment itself still happens later.
     */
    public int resolveXValue(ExtractedCosts costs, CostSelection selection, int announcedXValue) {
        if (costs.sacrificeAnyNumberCost() != null
                || costs.tapAnyNumberCost() != null
                || costs.returnAnyNumberCost() != null) {
            return selection.sacrificePermanentIds() == null ? 0 : selection.sacrificePermanentIds().size();
        }
        return announcedXValue;
    }

    /** Reads the card's additional cast costs without touching the card (for gating queries). */
    public ExtractedCosts peek(Card card) {
        return extractAndRemove(new ArrayList<>(card.getEffects(EffectSlot.SPELL)));
    }

    /** Reads printed and battlefield-granted additional cast costs without touching the card. */
    public ExtractedCosts peek(GameData gameData, UUID playerId, Card card) {
        return extractAndRemove(gameData, playerId, card, new ArrayList<>(card.getEffects(EffectSlot.SPELL)));
    }

    private <T extends CardEffect> T removeFirst(List<CardEffect> effects, Class<T> type) {
        T cost = effects.stream().filter(type::isInstance).map(type::cast).findFirst().orElse(null);
        if (cost != null) {
            effects.removeIf(type::isInstance);
        }
        return cost;
    }

    // ------------------------------------------------------------------
    // Satisfiability — can the costs be paid at all, with the best selection?
    // ------------------------------------------------------------------

    /**
     * True when every additional cast cost on the card could be paid by some selection right now.
     * The engine's single satisfiability query: the AI castability check, the MCTS simulator and
     * the playable-card computation all route here, so they can never disagree with the
     * validation below.
     */
    public boolean satisfiable(GameData gameData, UUID playerId, Card card) {
        return satisfiable(gameData, playerId, card, false);
    }

    /** True when the additional costs on a graveyard cast can be paid at all. */
    public boolean satisfiableForGraveyardCast(GameData gameData, UUID playerId, Card card) {
        return satisfiable(gameData, playerId, card, true);
    }

    private boolean satisfiable(GameData gameData, UUID playerId, Card card,
                                boolean includeFlashbackOnlyCosts) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        // Angel of Jubilation: life payments and creature sacrifices are unavailable as cast costs.
        boolean lifeAndSacAllowed = gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData);
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            switch (effect) {
                case SacrificeCreatureCost ignored -> {
                    if (!lifeAndSacAllowed) return false;
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case SacrificePermanentOrPayManaCost cost -> {
                    boolean hasPermanent = battlefield.stream().anyMatch(p ->
                            predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter())
                                    && (lifeAndSacAllowed || !gameQueryService.isCreature(gameData, p)));
                    if (!hasPermanent && !canAffordSacrificeOrPayManaOption(gameData, playerId, card, cost)) {
                        return false;
                    }
                }
                case SacrificeCreatureOrPayManaCost cost -> {
                    boolean hasCreature = lifeAndSacAllowed
                            && battlefield.stream().anyMatch(p -> gameQueryService.isCreature(gameData, p));
                    if (!hasCreature && !canAffordSacrificeOrPayManaOption(gameData, playerId, card,
                            new SacrificePermanentOrPayManaCost(
                                    cost.manaCost(), new PermanentIsCreaturePredicate(), "a creature"))) {
                        return false;
                    }
                }
                case SacrificePermanentOrDiscardCardCost cost -> {
                    boolean hasPermanent = battlefield.stream().anyMatch(p ->
                            predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter())
                                    && (!gameQueryService.isCreature(gameData, p) || lifeAndSacAllowed));
                    boolean hasDiscard = !discardCostIndices(gameData, playerId, card,
                            new DiscardCardTypeCost(null, null)).isEmpty();
                    if (!hasPermanent && !hasDiscard) return false;
                }
                case ExileCreatureCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case DiscardCardOrPayManaCost cost -> {
                    boolean hasDiscard = !discardCostIndices(gameData, playerId, card,
                            new DiscardCardTypeCost(null, null)).isEmpty();
                    if (!hasDiscard && !canAffordDiscardOrPayManaOption(gameData, playerId, card, cost)) {
                        return false;
                    }
                }
                case DiscardCardOrPayLifeCost cost -> {
                    boolean hasDiscard = !discardCostIndices(gameData, playerId, card,
                            new DiscardCardTypeCost(null, null)).isEmpty();
                    boolean canPayLife = lifeAndSacAllowed && gameData.getLife(playerId) >= cost.lifeAmount();
                    if (!hasDiscard && !canPayLife) {
                        return false;
                    }
                }
                case DiscardCardOrSacrificePermanentCost cost -> {
                    boolean hasDiscard = !discardCostIndices(gameData, playerId, card,
                            new DiscardCardTypeCost(null, null)).isEmpty();
                    boolean hasPermanent = battlefield.stream().anyMatch(p ->
                            predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter())
                                    && (lifeAndSacAllowed || !gameQueryService.isCreature(gameData, p)));
                    if (!hasDiscard && !hasPermanent) {
                        return false;
                    }
                }
                case ReturnCreatureToHandCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case ReturnPermanentToHandCost cost -> {
                    if (battlefield.stream().noneMatch(p ->
                            predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))) {
                        return false;
                    }
                }
                case BlightCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case PutCounterOnControlledCreatureCost cost -> {
                    if (!cost.optional()
                            && battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) {
                        return false;
                    }
                }
                case PutCountersOnControlledCreatureOrPayManaCost cost -> {
                    boolean hasCreature = battlefield.stream().anyMatch(p -> gameQueryService.isCreature(gameData, p));
                    if (!hasCreature && !canAffordPutCountersOrPayManaOption(gameData, playerId, card, cost)) {
                        return false;
                    }
                }
                case SacrificePermanentCost cost -> {
                    if (battlefield.stream().noneMatch(p ->
                            predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))) return false;
                }
                case SacrificeMultiplePermanentsCost cost -> {
                    long matching = battlefield.stream()
                            .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                            .count();
                    if (matching < cost.count()) return false;
                }
                case TapMultiplePermanentsCost cost -> {
                    int required = fixedTapCount(cost);
                    long matching = battlefield.stream()
                            .filter(p -> !p.isTapped())
                            .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                            .count();
                    if (matching < required) return false;
                }
                case WaterbendCost cost -> {
                    if (cost.optional()) {
                        continue;
                    }
                    int amount = cost.effectiveAmount(0);
                    long matching = battlefield.stream()
                            .filter(p -> !p.isTapped())
                            .filter(p -> gameQueryService.isArtifact(gameData, p)
                                    || gameQueryService.isCreature(gameData, p))
                            .count();
                    int availableMana = gameData.playerManaPools.getOrDefault(playerId, new ManaPool()).getTotalAllMana();
                    if (availableMana + Math.min(amount, (int) matching) < amount) return false;
                }
                case ExileNCardsFromGraveyardCost cost -> {
                    long matchingCount = graveyard.stream()
                            .filter(c -> (cost.requiredType() == null || c.hasType(cost.requiredType()))
                                    && (cost.predicate() == null
                                    || predicateEvaluationService.matchesCardPredicate(c, cost.predicate(), null)))
                            .count();
                    if (matchingCount < cost.count()) return false;
                }
                case ExileCardFromGraveyardCost cost -> {
                    if (graveyard.stream().noneMatch(c ->
                            (cost.requiredType() == null || c.hasType(cost.requiredType())
                                    || (cost.alternateType() != null && c.hasType(cost.alternateType())))
                                    && (cost.requiredSubtype() == null || c.getSubtypes().contains(cost.requiredSubtype())))) return false;
                }
                case ExileXCardsFromGraveyardCost cost -> {
                    if (graveyard.stream().noneMatch(c -> cost.requiredType() == null || c.hasType(cost.requiredType()))) return false;
                }
                case CollectEvidenceCost cost -> {
                    if (!cost.optional() && graveyard.stream().mapToInt(Card::getManaValue).sum()
                            < cost.minimumManaValue()) return false;
                }
                case DiscardCardTypeCost cost -> {
                    if (discardCostIndices(gameData, playerId, card, cost).size() < cost.count()) return false;
                }
                case RevealCardFromHandCost cost -> {
                    if (revealCardIndices(gameData, playerId, card, cost).isEmpty()) return false;
                }
                case DiscardRandomCardCost ignored -> {
                    if (hand.stream().noneMatch(candidate -> !candidate.getId().equals(card.getId()))) return false;
                }
                // Paying X life is always payable — X may be announced as 0.
                case PayXLifeCost ignored -> { }
                // Choosing X consumes no resource and is always satisfiable within its declared range.
                case ChooseXValueCost ignored -> { }
                case ChooseCreatureTypeCost ignored -> { }
                // A fixed life payment is only legal while the life total covers it (CR 119.4).
                case PayLifeCost cost -> {
                    if (!lifeAndSacAllowed) return false;
                    int life = gameData.getLife(playerId);
                    if (life < cost.effectiveAmount(life)) return false;
                }
                case PayLifeOrSacrificePermanentCost cost -> {
                    int life = gameData.getLife(playerId);
                    boolean canPayLife = lifeAndSacAllowed && life >= cost.lifeAmount();
                    boolean canSacrifice = battlefield.stream()
                            .filter(p -> predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, p, cost.filter()))
                            .anyMatch(p -> !gameQueryService.isCreature(gameData, p) || lifeAndSacAllowed);
                    if (!canPayLife && !canSacrifice) return false;
                }
                case PayLifeOrPayManaCost cost -> {
                    int life = gameData.getLife(playerId);
                    boolean canPayLife = lifeAndSacAllowed && life >= cost.lifeAmount();
                    boolean canPayMana = canAffordManaOption(gameData, playerId, card, cost.manaCost());
                    if (!canPayLife && !canPayMana) return false;
                }
                // Escalate is payable with a single mode (zero extra payments), so it never blocks
                // playability by itself — concrete mode+payment selections are validated at cast.
                case EscalateDiscardCost ignored -> { }
                case EscalateManaCost ignored -> { }
                case TieredManaCost ignored -> { }
                case EscalateSacrificeCost ignored -> { }
                case EscalateTapCost ignored -> { }
                // Sacrificing all creatures / permanents you control is legal with zero.
                case SacrificeAllCreaturesYouControlCost ignored -> { }
                case SacrificeAllPermanentsYouControlCost ignored -> { }
                // Discarding your entire hand is legal with an empty hand.
                case DiscardHandCost ignored -> { }
                // "Discard X cards" is payable with X = 0, so it never blocks a cast on its own;
                // the announced X is checked against the hand by validateDiscardXCardsCost.
                case DiscardXCardsCost ignored -> { }
                // Tapping / returning "any number of" permanents is payable with zero, so it never
                // blocks a cast.
                case SacrificeAnyNumberOfPermanentsCost ignored -> { }
                case TapAnyNumberOfPermanentsCost ignored -> { }
                case ReturnAnyNumberOfPermanentsToHandCost ignored -> { }
                case BeholdAndExileCost cost -> {
                    boolean hasPermanent = battlefield.stream().anyMatch(p ->
                            predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, p, new PermanentHasSubtypePredicate(cost.subtype())));
                    boolean hasHandCard = hand.stream().anyMatch(c ->
                            !c.getId().equals(card.getId())
                                    && predicateEvaluationService.matchesCardPredicate(
                                    c, new CardSubtypePredicate(cost.subtype()), c.getId()));
                    if (!hasPermanent && !hasHandCard) return false;
                }
                // Fixed BeholdCost is a flashback-only additional cost. Optional chosen-type
                // BeholdCost is payable by declining the optional cost.
                case BeholdCost cost -> {
                    if (includeFlashbackOnlyCosts && !cost.optional() && !cost.chosenCreatureType()) {
                        long matchingPermanents = battlefield.stream()
                                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(
                                        gameData, p, new PermanentHasSubtypePredicate(cost.subtype())))
                                .count();
                        long matchingHandCards = hand.stream()
                                .filter(c -> !c.getId().equals(card.getId()))
                                .filter(c -> predicateEvaluationService.matchesCardPredicate(
                                        c, new CardSubtypePredicate(cost.subtype()), c.getId()))
                                .count();
                        if (matchingPermanents + matchingHandCards < cost.count()) return false;
                    }
                }
                default -> { }
            }
        }
        return true;
    }

    /**
     * Hand indices (as the caller/UI sees them, with the spell still in hand) whose card can pay
     * the spell's "discard a card" additional cast cost (plain discard, or the discard option of
     * discard-or-pay-mana). Returns {@code null} when the card has no such cost, an empty list when
     * the cost exists but is unpayable via discard. Any card other than the spell itself that
     * matches the cost's predicate qualifies (CR 601.2b — the spell is on the stack when costs are
     * paid, so it can never be its own discard).
     */
    public List<Integer> validDiscardCostIndices(GameData gameData, UUID playerId, Card card) {
        ExtractedCosts costs = peek(card);
        if (costs.discardCost() != null) {
            return discardCostIndices(gameData, playerId, card, costs.discardCost());
        }
        if (costs.discardCardOrPayManaCost() != null) {
            return discardCostIndices(gameData, playerId, card, new DiscardCardTypeCost(null, null));
        }
        if (costs.sacrificePermanentOrDiscardCardCost() != null) {
            return discardCostIndices(gameData, playerId, card, new DiscardCardTypeCost(null, null));
        }
        if (costs.discardCardOrPayLifeCost() != null) {
            return discardCostIndices(gameData, playerId, card, new DiscardCardTypeCost(null, null));
        }
        if (costs.discardCardOrSacrificePermanentCost() != null) {
            return discardCostIndices(gameData, playerId, card, new DiscardCardTypeCost(null, null));
        }
        if (costs.revealCardCost() != null) {
            return revealCardIndices(gameData, playerId, card, costs.revealCardCost());
        }
        return null;
    }

    private List<Integer> revealCardIndices(GameData gameData, UUID playerId, Card card,
                                            RevealCardFromHandCost cost) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card candidate = hand.get(i);
            if (!candidate.getId().equals(card.getId())
                    && (cost.predicate() == null
                    || predicateEvaluationService.matchesCardPredicate(candidate, cost.predicate(), candidate.getId()))) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> discardCostIndices(GameData gameData, UUID playerId, Card card, DiscardCardTypeCost cost) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card candidate = hand.get(i);
            if (candidate.getId().equals(card.getId())) continue;
            if (cost.predicate() != null
                    && !predicateEvaluationService.matchesCardPredicate(candidate, cost.predicate(), candidate.getId())) continue;
            indices.add(i);
        }
        return indices;
    }

    // ------------------------------------------------------------------
    // Validation — is this concrete selection a legal payment? Mutates nothing.
    // ------------------------------------------------------------------

    /**
     * Validates every extracted cost against the caster's selection, in canonical payment order,
     * throwing {@link IllegalStateException} on the first unpayable one. Mutates nothing — call
     * before any cost (mana included) is paid.
     */
    public void validateAll(GameData gameData, Player player, Card card,
                            ExtractedCosts costs, CostSelection selection) {
        validateAll(gameData, player, card, costs, selection, null, null);
    }

    /**
     * Validates additional costs while retaining the announced X value for costs whose amount
     * must match the spell's mana-cost X.
     */
    public void validateAll(GameData gameData, Player player, Card card,
                            ExtractedCosts costs, CostSelection selection, Integer announcedXValue) {
        validateAll(gameData, player, card, costs, selection, announcedXValue, true);
    }

    /** Validates additional costs while recording whether an optional waterbend cost was chosen. */
    public void validateAll(GameData gameData, Player player, Card card,
                            ExtractedCosts costs, CostSelection selection, Integer announcedXValue,
                            boolean waterbendPaid) {
        validateAll(gameData, player, card, costs, selection, announcedXValue, waterbendPaid, null);
    }

    public void validateAll(GameData gameData, Player player, Card card,
                            ExtractedCosts costs, CostSelection selection, Integer announcedXValue,
                            Integer resolvedCollectEvidenceMinimumManaValue) {
        validateAll(gameData, player, card, costs, selection, announcedXValue, true,
                resolvedCollectEvidenceMinimumManaValue);
    }

    /**
     * Validates additional costs using both the optional waterbend choice and any target-based
     * evidence threshold locked in when the spell's targets were chosen.
     */
    public void validateAll(GameData gameData, Player player, Card card,
                            ExtractedCosts costs, CostSelection selection, Integer announcedXValue,
                            boolean waterbendPaid, Integer resolvedCollectEvidenceMinimumManaValue) {
        if (costs.payLifeCost() != null) {
            validatePayLifeCost(gameData, player, card, costs.payLifeCost());
        }
        if (costs.payLifeOrSacrificePermanentCost() != null) {
            PayLifeOrSacrificePermanentCost cost = costs.payLifeOrSacrificePermanentCost();
            if (selection.sacrificePermanentId() != null) {
                Permanent permanent = validateSingleSacrificeCost(gameData, player, card,
                        selection.sacrificePermanentId(), "a creature or enchantment",
                        p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
                if (gameQueryService.isCreature(gameData, permanent)) {
                    validateCanSacrificeCreatureForCost(gameData, card);
                }
            } else {
                validateCanPayLifeForCost(gameData, card);
                int life = gameData.getLife(player.getId());
                if (life < cost.lifeAmount()) {
                    throw new IllegalStateException("Not enough life to pay " + cost.lifeAmount()
                            + " life for " + card.getName());
                }
            }
        }
        if (costs.payLifeOrPayManaCost() != null) {
            PayLifeOrPayManaCost cost = costs.payLifeOrPayManaCost();
            if (Boolean.TRUE.equals(selection.payLifeForAdditionalCost())) {
                validateCanPayLifeForCost(gameData, card);
                int life = gameData.getLife(player.getId());
                if (life < cost.lifeAmount()) {
                    throw new IllegalStateException("Not enough life to pay " + cost.lifeAmount()
                            + " life for " + card.getName());
                }
            } else if (!canAffordManaOption(gameData, player.getId(), card, cost.manaCost())) {
                throw new IllegalStateException("Must pay " + cost.lifeAmount() + " life or "
                        + cost.manaCost() + " to cast " + card.getName());
            }
        }
        if (costs.discardCardOrSacrificePermanentCost() != null) {
            DiscardCardOrSacrificePermanentCost cost = costs.discardCardOrSacrificePermanentCost();
            if ((selection.discardHandCardIndex() != null)
                    == (selection.sacrificePermanentId() != null)) {
                throw new IllegalStateException("Must discard a card or sacrifice "
                        + cost.description() + " to cast " + card.getName());
            }
            if (selection.discardHandCardIndex() != null) {
                validateDiscardCost(gameData, player, card, new DiscardCardTypeCost(null, null),
                        selection.discardHandCardIndex(), selection.spellCardIndex());
            } else {
                Permanent selected = validateSingleSacrificeCost(gameData, player, card,
                        selection.sacrificePermanentId(), cost.description(),
                        p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
                if (gameQueryService.isCreature(gameData, selected)) {
                    validateCanSacrificeCreatureForCost(gameData, card);
                }
            }
        }
        if (costs.sacrificeCreature()) {
            validateCanSacrificeCreatureForCost(gameData, card);
            validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    "a creature", p -> gameQueryService.isCreature(gameData, p));
        }
        if (costs.sacrificePermanentOrPayManaCost() != null) {
            if (selection.sacrificePermanentId() != null) {
                Permanent selected = gameQueryService.findPermanentById(gameData, selection.sacrificePermanentId());
                if (selected != null && gameQueryService.isCreature(gameData, selected)) {
                    validateCanSacrificeCreatureForCost(gameData, card);
                }
                validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                        costs.sacrificePermanentOrPayManaCost().description(),
                        p -> predicateEvaluationService.matchesPermanentPredicate(
                                gameData, p, costs.sacrificePermanentOrPayManaCost().filter()));
            } else if (!canAffordSacrificeOrPayManaOption(gameData, player.getId(), card,
                    costs.sacrificePermanentOrPayManaCost())) {
                throw new IllegalStateException("Must sacrifice "
                        + costs.sacrificePermanentOrPayManaCost().description() + " or pay "
                        + costs.sacrificePermanentOrPayManaCost().manaCost()
                        + " to cast " + card.getName());
            }
        }
        if (costs.sacrificePermanentOrDiscardCardCost() != null) {
            SacrificePermanentOrDiscardCardCost cost = costs.sacrificePermanentOrDiscardCardCost();
            if (selection.sacrificePermanentId() != null) {
                Permanent selected = gameQueryService.findPermanentById(gameData, selection.sacrificePermanentId());
                if (selected != null && gameQueryService.isCreature(gameData, selected)) {
                    validateCanSacrificeCreatureForCost(gameData, card);
                }
                validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                        cost.description(),
                        p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
            } else if (selection.discardHandCardIndex() != null) {
                validateDiscardCost(gameData, player, card, new DiscardCardTypeCost(null, null),
                        selection.discardHandCardIndex(), selection.spellCardIndex());
            } else {
                throw new IllegalStateException("Must sacrifice " + cost.description()
                        + " or discard a card to cast " + card.getName());
            }
        }
        if (costs.discardCardOrPayManaCost() != null) {
            if (selection.discardHandCardIndex() != null) {
                validateDiscardCost(gameData, player, card, new DiscardCardTypeCost(null, null),
                        selection.discardHandCardIndex(), selection.spellCardIndex());
            } else if (!canAffordDiscardOrPayManaOption(gameData, player.getId(), card,
                    costs.discardCardOrPayManaCost())) {
                throw new IllegalStateException("Must discard a card or pay "
                        + costs.discardCardOrPayManaCost().manaCost()
                        + " to cast " + card.getName());
            }
        }
        if (costs.discardCardOrPayLifeCost() != null) {
            if (selection.discardHandCardIndex() != null) {
                validateDiscardCost(gameData, player, card, new DiscardCardTypeCost(null, null),
                        selection.discardHandCardIndex(), selection.spellCardIndex());
            } else {
                validateCanPayLifeForCost(gameData, card);
                int life = gameData.getLife(player.getId());
                if (life < costs.discardCardOrPayLifeCost().lifeAmount()) {
                    throw new IllegalStateException("Not enough life to pay "
                            + costs.discardCardOrPayLifeCost().lifeAmount()
                            + " for " + card.getName());
                }
            }
        }
        if (costs.revealCardCost() != null) {
            validateRevealCardCost(gameData, player, card, costs.revealCardCost(),
                    selection.discardHandCardIndex(), selection.spellCardIndex());
        }
        if (costs.sacrificePermanentCost() != null) {
            validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    costs.sacrificePermanentCost().description(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, costs.sacrificePermanentCost().filter()));
        }
        if (costs.exileCreatureCost() != null) {
            validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    "a creature", p -> gameQueryService.isCreature(gameData, p));
        }
        if (costs.sacrificeMultiplePermanentsCost() != null) {
            validateMultipleSacrificeCost(gameData, player, card, costs.sacrificeMultiplePermanentsCost(),
                    selection.sacrificePermanentIds());
        }
        if (costs.escalateSacrificeCost() != null) {
            validateEscalateSacrificeCost(gameData, player, card, costs.escalateSacrificeCost(),
                    selection.escalateModeCount(), selection.sacrificePermanentIds());
        }
        if (costs.escalateTapCost() != null) {
            validateEscalateTapCost(gameData, player, card, costs.escalateTapCost(),
                    selection.escalateModeCount(), selection.sacrificePermanentIds());
        }
        if (costs.sacrificeAnyNumberCost() != null) {
            validateSacrificeAnyNumberOfPermanentsCost(gameData, player, card, costs.sacrificeAnyNumberCost(),
                    selection.sacrificePermanentIds());
        }
        if (costs.tapAnyNumberCost() != null) {
            validateTapAnyNumberOfPermanentsCost(gameData, player, card, costs.tapAnyNumberCost(),
                    selection.sacrificePermanentIds());
        }
        if (costs.tapMultipleCost() != null) {
            validateTapMultiplePermanentsCost(gameData, player, card, costs.tapMultipleCost(),
                    selection.sacrificePermanentIds(), announcedXValue == null ? 0 : announcedXValue);
        }
        if (costs.waterbendCost() != null
                && (!costs.waterbendCost().optional() || waterbendPaid)) {
            validateWaterbendCost(gameData, player, card, costs.waterbendCost(),
                    selection.sacrificePermanentIds(), announcedXValue == null ? 0 : announcedXValue);
        }
        if (costs.returnAnyNumberCost() != null) {
            validateReturnAnyNumberOfPermanentsToHandCost(gameData, player, card, costs.returnAnyNumberCost(),
                    selection.sacrificePermanentIds());
        }
        if (costs.returnPermanentToHand() != null) {
            validateReturnPermanentToHandCost(gameData, player, card, costs.returnPermanentToHand(),
                    selection.sacrificePermanentId());
        }
        // Sacrificing all creatures / permanents you control has no failure mode (zero is legal).
        // Discarding your entire hand has no failure mode (empty hand is legal).
        if (costs.returnCreatureToHand()) {
            validateReturnCreatureToHandCost(gameData, player, card, selection.sacrificePermanentId());
        }
        if (costs.blightCost() != null) {
            validateBlightCost(gameData, player, card, selection.sacrificePermanentId());
        }
        if (costs.putCounterCost() != null
                && (!costs.putCounterCost().optional() || selection.sacrificePermanentId() != null)) {
            validatePutCounterOnControlledCreatureCost(gameData, player, card, costs.putCounterCost(),
                    selection.sacrificePermanentId());
        }
        if (costs.putCountersOrPayManaCost() != null) {
            if (selection.sacrificePermanentId() != null) {
                validatePutCountersOnControlledCreatureOrPayManaCost(gameData, player, card,
                        costs.putCountersOrPayManaCost(), selection.sacrificePermanentId());
            } else if (!canAffordPutCountersOrPayManaOption(gameData, player.getId(), card,
                    costs.putCountersOrPayManaCost())) {
                throw new IllegalStateException("Must put counters on a creature you control or pay "
                        + costs.putCountersOrPayManaCost().manaCost() + " to cast " + card.getName());
            }
        }
        if (costs.exileGraveyardCost() != null) {
            validateExileGraveyardCost(gameData, player, card, costs.exileGraveyardCost(),
                    selection.exileGraveyardCardIndex());
        }
        if (costs.exileXCardsCost() != null) {
            validateExileXCardsFromGraveyardCost(gameData, player, card, costs.exileXCardsCost(),
                    selection.exileGraveyardCardIndices(), announcedXValue);
        }
        if (costs.collectEvidenceCost() != null
                && (!costs.collectEvidenceCost().optional()
                || (selection.exileGraveyardCardIndices() != null
                && !selection.exileGraveyardCardIndices().isEmpty()))) {
            int minimumManaValue = costs.collectEvidenceCost().usesTargetManaValue()
                    ? requireResolvedCollectEvidenceMinimum(resolvedCollectEvidenceMinimumManaValue, card)
                    : costs.collectEvidenceCost().minimumManaValue();
            validateCollectEvidenceCost(gameData, player, card, costs.collectEvidenceCost(),
                    selection.exileGraveyardCardIndices(), minimumManaValue);
        }
        if (costs.exileNCardsCost() != null) {
            validateExileNCardsFromGraveyardCost(gameData, player, card, costs.exileNCardsCost(),
                    selection.exileGraveyardCardIndices(), -1);
        }
        if (costs.delveCost() != null) {
            validateDelveCost(gameData, player, card, costs.delveCost(), selection.exileGraveyardCardIndices());
        }
        if (costs.discardCost() != null) {
            if (costs.discardCost().count() == 1) {
                validateDiscardCost(gameData, player, card, costs.discardCost(),
                        selection.discardHandCardIndex(), selection.spellCardIndex());
            } else {
                validateDiscardCardsCost(gameData, player, card, costs.discardCost(),
                        selection.discardHandCardIndices(), selection.spellCardIndex());
            }
        }
        if (costs.discardRandomCost() != null) {
            validateRandomDiscardCost(gameData, player, card);
        }
        if (costs.escalateDiscardCost() != null) {
            validateEscalateDiscardCost(gameData, player, card, selection.escalateModeCount(),
                    selection.discardHandCardIndices(), selection.spellCardIndex());
        }
        if (costs.escalateManaCost() != null) {
            validateEscalateManaCost(card, costs.escalateManaCost(), selection.escalateModeCount());
        }
        if (costs.tieredManaCost() != null) {
            validateTieredManaCost(card, costs.tieredManaCost(), announcedXValue == null ? 0 : announcedXValue);
        }
        if (costs.chooseXValueCost() != null) {
            validateChooseXValueCost(card, costs.chooseXValueCost(), announcedXValue != null ? announcedXValue : 0);
        }
        if (costs.beholdCost() != null) {
            validateBeholdCost(gameData, player, card, costs.beholdCost(), selection);
        }
        if (costs.beholdSelectionCost() != null && costs.beholdSelectionCost().chosenCreatureType()) {
            validateBeholdCost(gameData, player, card, costs.beholdSelectionCost(), selection);
        }
        if (costs.chooseCreatureTypeCost() != null) {
            validateChooseCreatureTypeCost(gameData, card, selection.beholdChosenSubtype());
        }
    }

    /** Validates the creature subtype chosen as an additional cast cost. */
    public void validateChooseCreatureTypeCost(GameData gameData, Card card, CardSubtype chosenSubtype) {
        if (chosenSubtype == null || !gameQueryService.isCreatureSubtype(chosenSubtype)) {
            throw new IllegalStateException("Choose a creature type to cast " + card.getName());
        }
    }

    public Card validateBeholdCost(GameData gameData, Player player, Card card,
                                   BeholdAndExileCost cost, CostSelection selection) {
        boolean hasPermanentChoice = selection.beholdPermanentId() != null;
        boolean hasHandChoice = selection.beholdHandCardIndex() != null;
        if (hasPermanentChoice == hasHandChoice) {
            throw new IllegalStateException("Choose either a matching permanent or a matching card from hand to cast "
                    + card.getName());
        }
        if (hasPermanentChoice) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, selection.beholdPermanentId());
            if (permanent == null || !player.getId().equals(
                    gameQueryService.findPermanentController(gameData, permanent.getId()))) {
                throw new IllegalStateException("Must behold a permanent you control to cast " + card.getName());
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(
                    gameData, permanent, new PermanentHasSubtypePredicate(cost.subtype()))) {
                throw new IllegalStateException("Beheld permanent must be a " + cost.subtype().name().toLowerCase());
            }
            return permanent.getOriginalCard();
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        int selectedIndex = selection.beholdHandCardIndex();
        if (selectedIndex == selection.spellCardIndex() || hand == null) {
            throw new IllegalStateException("Must behold a matching card from hand to cast " + card.getName());
        }
        int effectiveIndex = selection.spellCardIndex() >= 0 && selectedIndex > selection.spellCardIndex()
                ? selectedIndex - 1 : selectedIndex;
        if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
            throw new IllegalStateException("Invalid hand card selected for behold");
        }
        Card beheldCard = hand.get(effectiveIndex);
        if (!predicateEvaluationService.matchesCardPredicate(
                beheldCard, new CardSubtypePredicate(cost.subtype()), beheldCard.getId())) {
            throw new IllegalStateException("Beheld card must be a " + cost.subtype().name().toLowerCase());
        }
        return beheldCard;
    }

    public void validateBeholdCost(GameData gameData, Player player, Card card,
                                   BeholdCost cost, CostSelection selection) {
        if (cost.chosenCreatureType()) {
            validateBeholdSelectionCost(gameData, player, card, cost, selection);
            return;
        }
        List<UUID> permanentIds = selection.beholdPermanentIds() != null
                ? selection.beholdPermanentIds() : List.of();
        List<Integer> handIndices = selection.beholdHandCardIndices() != null
                ? selection.beholdHandCardIndices() : List.of();
        if (permanentIds.isEmpty() && handIndices.isEmpty() && cost.optional()) {
            return;
        }
        if (permanentIds.size() + handIndices.size() != cost.count()) {
            throw new IllegalStateException("Must behold exactly " + cost.count() + " "
                    + cost.subtype().name().toLowerCase());
        }
        if (Set.copyOf(permanentIds).size() != permanentIds.size()) {
            throw new IllegalStateException("Cannot behold the same permanent more than once");
        }
        if (Set.copyOf(handIndices).size() != handIndices.size()) {
            throw new IllegalStateException("Cannot behold the same card more than once");
        }
        Set<UUID> chosenObjectIds = new java.util.HashSet<>(permanentIds);
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !player.getId().equals(
                    gameQueryService.findPermanentController(gameData, permanentId))) {
                throw new IllegalStateException("Must behold a permanent you control");
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(
                    gameData, permanent, new PermanentHasSubtypePredicate(cost.subtype()))) {
                throw new IllegalStateException("Beheld permanent must be a " + cost.subtype().name().toLowerCase());
            }
        }

        List<Card> hand = gameData.playerHands.getOrDefault(player.getId(), List.of());
        for (Integer selectedIndex : handIndices) {
            if (selectedIndex == null) {
                throw new IllegalStateException("Invalid hand card selected for behold");
            }
            int effectiveIndex = selection.spellCardIndex() >= 0 && selectedIndex > selection.spellCardIndex()
                    ? selectedIndex - 1 : selectedIndex;
            if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
                throw new IllegalStateException("Invalid hand card selected for behold");
            }
            Card beheldCard = hand.get(effectiveIndex);
            if (beheldCard.getId().equals(card.getId())
                    || !predicateEvaluationService.matchesCardPredicate(
                    beheldCard, new CardSubtypePredicate(cost.subtype()), beheldCard.getId())) {
                throw new IllegalStateException("Beheld card must be a " + cost.subtype().name().toLowerCase());
            }
            if (!chosenObjectIds.add(beheldCard.getId())) {
                throw new IllegalStateException("Cannot behold the same object more than once");
            }
        }
    }

    private void validateBeholdSelectionCost(GameData gameData, Player player, Card card,
                                             BeholdCost cost, CostSelection selection) {
        List<UUID> permanentIds = selection.beholdPermanentIds() != null
                ? selection.beholdPermanentIds() : List.of();
        List<Integer> handIndices = selection.beholdHandCardIndices() != null
                ? selection.beholdHandCardIndices() : List.of();
        if (permanentIds.isEmpty() && handIndices.isEmpty() && cost.optional()
                && selection.beholdChosenSubtype() == null) {
            return;
        }
        if (permanentIds.size() + handIndices.size() != cost.count()) {
            throw new IllegalStateException("Must behold exactly " + cost.count() + " creatures of the chosen type");
        }
        CardSubtype chosenSubtype = selection.beholdChosenSubtype();
        if (chosenSubtype == null) {
            throw new IllegalStateException("Choose a creature type to pay the behold cost");
        }
        Set<UUID> chosenObjectIds = new java.util.HashSet<>();
        for (UUID permanentId : permanentIds) {
            if (permanentId == null || !chosenObjectIds.add(permanentId)) {
                throw new IllegalStateException("Cannot behold the same object more than once");
            }
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !player.getId().equals(
                    gameQueryService.findPermanentController(gameData, permanentId))
                    || !gameQueryService.isCreature(gameData, permanent)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    gameData, permanent, new PermanentHasSubtypePredicate(chosenSubtype))) {
                throw new IllegalStateException("Beheld permanents must be creatures you control of the chosen type");
            }
        }

        List<Card> hand = gameData.playerHands.getOrDefault(player.getId(), List.of());
        for (Integer selectedIndex : handIndices) {
            if (selectedIndex == null) {
                throw new IllegalStateException("Invalid hand card selected for behold");
            }
            int effectiveIndex = selection.spellCardIndex() >= 0 && selectedIndex > selection.spellCardIndex()
                    ? selectedIndex - 1 : selectedIndex;
            if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
                throw new IllegalStateException("Invalid hand card selected for behold");
            }
            Card beheldCard = hand.get(effectiveIndex);
            if (beheldCard.getId().equals(card.getId())
                    || !beheldCard.hasType(CardType.CREATURE)
                    || !predicateEvaluationService.matchesCardPredicate(
                    beheldCard, new CardSubtypePredicate(chosenSubtype), beheldCard.getId())) {
                throw new IllegalStateException("Beheld cards must be creatures of the chosen type");
            }
            if (!chosenObjectIds.add(beheldCard.getId())) {
                throw new IllegalStateException("Cannot behold the same object more than once");
            }
        }
    }

    /** Validates the caster's chosen X against a range declared by an additional cast cost. */
    public void validateChooseXValueCost(Card card, ChooseXValueCost cost, int chosenXValue) {
        if (chosenXValue < cost.minValue() || chosenXValue > cost.maxValue()) {
            throw new IllegalStateException("X must be between " + cost.minValue() + " and "
                    + cost.maxValue() + " for " + card.getName());
        }
    }

    /**
     * Validates a fixed "pay N life" additional cast cost (Fumarole). A player may pay life only
     * while their life total is at least the amount paid (CR 119.4).
     */
    public void validatePayLifeCost(GameData gameData, Player player, Card card, PayLifeCost cost) {
        validateCanPayLifeForCost(gameData, card);
        int life = gameData.getLife(player.getId());
        int amount = cost.effectiveAmount(life);
        if (life < amount) {
            throw new IllegalStateException("Not enough life to pay " + amount + " life for " + card.getName());
        }
    }

    /**
     * Angel of Jubilation: a life payment can't be used as a cost of casting a spell.
     */
    private void validateCanPayLifeForCost(GameData gameData, Card card) {
        if (!gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData)) {
            throw new IllegalStateException("Players can't pay life to cast " + card.getName());
        }
    }

    /**
     * Angel of Jubilation: a creature sacrifice can't be used as a cost of casting a spell.
     */
    private void validateCanSacrificeCreatureForCost(GameData gameData, Card card) {
        if (!gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData)) {
            throw new IllegalStateException("Players can't sacrifice creatures to cast " + card.getName());
        }
    }

    /**
     * Validates the "pay X life" additional cast cost (Fire Covenant) against the announced X.
     * A player may pay life only while their life total is at least the amount paid (CR 119.4).
     * Kept out of {@link #validateAll} because only the cast path knows the announced X.
     */
    public void validatePayXLifeCost(GameData gameData, Player player, Card card, int announcedX) {
        if (announcedX > 0) {
            validateCanPayLifeForCost(gameData, card);
        }
        if (announcedX < 0) {
            throw new IllegalStateException("X cannot be negative for " + card.getName());
        }
        if (gameData.getLife(player.getId()) < announcedX) {
            throw new IllegalStateException("Not enough life to pay " + announcedX + " life for " + card.getName());
        }
    }

    /**
     * Builds the mana-symbol suffix paid for escalate (the escalate cost repeated once per mode
     * beyond the first). Empty when there is no escalate mana cost or only one mode is chosen.
     */
    public String escalateManaSuffix(EscalateManaCost cost, int modesChosen) {
        if (cost == null || cost.manaCost() == null || cost.manaCost().isEmpty()) {
            return "";
        }
        int times = Math.max(0, modesChosen - 1);
        if (times == 0) {
            return "";
        }
        return cost.manaCost().repeat(times);
    }

    /** Builds the selected mode's additional mana-cost suffix for a tiered spell. */
    public String tieredManaSuffix(TieredManaCost cost, int modeIndex) {
        if (cost == null) {
            return "";
        }
        if (modeIndex < 0 || modeIndex >= cost.additionalManaCosts().size()) {
            throw new IllegalStateException("Invalid tiered mode index: " + modeIndex);
        }
        return cost.additionalManaCosts().get(modeIndex);
    }

    /** Validates the selected mode for a tiered spell's additional mana cost. */
    public void validateTieredManaCost(Card card, TieredManaCost cost, int modeIndex) {
        if (modeIndex < 0 || modeIndex >= cost.additionalManaCosts().size()) {
            throw new IllegalStateException("Invalid tiered mode for " + card.getName());
        }
    }

    /**
     * Builds the mana-symbol suffix paid for a {@link RepeatableAdditionalManaCost} — the caster's
     * chosen payments concatenated, so the normal mana path pays them as part of the spell's total
     * cost (CR 601.2f–h), exactly as escalate's suffix is paid. Rejects a payment that is not one
     * of the cost's declared options, and any payment at all on a spell without the cost.
     */
    public String repeatedAdditionalCostSuffix(Card card, RepeatableAdditionalManaCost cost, List<String> payments) {
        if (payments == null || payments.isEmpty()) {
            return "";
        }
        if (cost == null) {
            throw new IllegalStateException(card.getName() + " has no repeatable additional cost to pay");
        }
        int[] paymentCounts = new int[cost.paymentOptions().size()];
        for (String payment : payments) {
            int optionIndex = -1;
            int matchingOptionIndex = -1;
            for (int i = 0; i < cost.paymentOptions().size(); i++) {
                RepeatableAdditionalManaCost.PaymentOption option = cost.paymentOptions().get(i);
                if (option.manaCost().equals(payment)) {
                    matchingOptionIndex = i;
                    if (paymentCounts[i] < option.maxPayments()) {
                        optionIndex = i;
                        break;
                    }
                }
            }
            if (optionIndex < 0) {
                if (matchingOptionIndex >= 0
                        && cost.paymentOptions().get(matchingOptionIndex).maxPayments() != Integer.MAX_VALUE) {
                    throw new IllegalStateException("Additional cost payment " + payment
                            + " may be paid at most "
                            + cost.paymentOptions().get(matchingOptionIndex).maxPayments() + " time(s) for "
                            + card.getName());
                }
                throw new IllegalStateException("Invalid additional cost payment " + payment
                        + " for " + card.getName());
            }
            paymentCounts[optionIndex]++;
        }
        return String.join("", payments);
    }

    /**
     * Validates escalate's mana-cost-per-extra-mode declaration (CR 702.124). Mana affordability is
     * checked by the cast path as part of the spell's total cost; this only guards the mode count.
     */
    public void validateEscalateManaCost(Card card, EscalateManaCost cost, int modesChosen) {
        if (modesChosen < 1) {
            throw new IllegalStateException("Must choose at least one mode to cast " + card.getName());
        }
        if (cost.manaCost() == null || cost.manaCost().isEmpty()) {
            throw new IllegalStateException("Escalate mana cost is missing on " + card.getName());
        }
    }

    /**
     * True when the pool can pay the spell's mana cost plus the alternate mana option (no cost
     * modifiers applied — cast-time payment re-checks after modifiers via the normal mana path).
     */
    public boolean canAffordSacrificeOrPayManaOption(GameData gameData, UUID playerId, Card card,
                                                     SacrificePermanentOrPayManaCost cost) {
        return canAffordManaOption(gameData, playerId, card, cost.manaCost());
    }

    /**
     * True when the pool can pay the spell's mana cost plus the discard-or-pay alternate mana
     * option (no cost modifiers applied — cast-time payment re-checks after modifiers).
     */
    public boolean canAffordDiscardOrPayManaOption(GameData gameData, UUID playerId, Card card,
                                                   DiscardCardOrPayManaCost cost) {
        return canAffordManaOption(gameData, playerId, card, cost.manaCost());
    }

    /** True when the pool can pay the spell's mana cost plus the counter-cost mana option. */
    public boolean canAffordPutCountersOrPayManaOption(GameData gameData, UUID playerId, Card card,
                                                       PutCountersOnControlledCreatureOrPayManaCost cost) {
        return canAffordManaOption(gameData, playerId, card, cost.manaCost());
    }

    private boolean canAffordManaOption(GameData gameData, UUID playerId, Card card, String optionManaCost) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool == null) {
            return false;
        }
        String base = card.getManaCost() != null ? card.getManaCost() : "";
        return new ManaCost(base + optionManaCost).canPay(pool);
    }

    /**
     * Runs the single-sacrifice legality checks without mutating anything. Returns the permanent
     * that would be sacrificed. Also used for kicker sacrifice costs.
     */
    public Permanent validateSingleSacrificeCost(GameData gameData, Player player, Card sourceCard,
                                                 UUID sacrificePermanentId, String typeDescription,
                                                 Predicate<Permanent> typeCheck) {
        if (sacrificePermanentId == null) {
            throw new IllegalStateException("Must sacrifice " + typeDescription + " to cast " + sourceCard.getName());
        }
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, sacrificePermanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Sacrifice target not found on battlefield");
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, sacrificePermanentId);
        if (!player.getId().equals(controllerId)) {
            throw new IllegalStateException("Can only sacrifice permanents you control");
        }
        if (!typeCheck.test(toSacrifice)) {
            throw new IllegalStateException("Sacrifice target must be " + typeDescription);
        }
        return toSacrifice;
    }

    /** Validates a graveyard casting cost that removes counters from controlled creatures. */
    public void validateRemoveCountersFromControlledCreaturesCost(
            GameData gameData, Player player, Card card,
            RemoveCountersFromControlledCreaturesCastingCost cost, List<UUID> permanentIds) {
        if (cost == null) {
            return;
        }
        if (cost.count() < 1) {
            throw new IllegalStateException("The counter cost must remove at least one counter");
        }
        List<UUID> ids = permanentIds != null ? permanentIds : List.of();
        if (ids.size() != cost.count()) {
            throw new IllegalStateException("Must remove exactly " + cost.count()
                    + " counters to cast " + card.getName());
        }

        HashMap<UUID, Integer> selectedCounts = new HashMap<>();
        for (UUID id : ids) {
            if (id == null) {
                throw new IllegalStateException("Invalid creature selected for the counter cost");
            }
            selectedCounts.merge(id, 1, Integer::sum);
        }
        for (var selected : selectedCounts.entrySet()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, selected.getKey());
            if (permanent == null
                    || !player.getId().equals(gameQueryService.findPermanentController(gameData, selected.getKey()))
                    || !gameQueryService.isCreature(gameData, permanent)) {
                throw new IllegalStateException("Counters must be removed from creatures you control");
            }
            if (counterCount(permanent, cost.counterType()) < selected.getValue()) {
                throw new IllegalStateException("Creature does not have enough counters to pay "
                        + card.getName() + "'s cost");
            }
        }
    }

    /** Validates a variable counter cost on a graveyard cast. */
    public void validateRemoveXCountersFromControlledPermanentsCost(
            GameData gameData, Player player, Card card,
            RemoveXCountersFromControlledPermanentsCastingCost cost, int xValue,
            List<UUID> permanentIds) {
        if (cost == null) {
            return;
        }
        if (xValue < 1) {
            throw new IllegalStateException("X must be greater than zero for " + card.getName()
                    + "'s counter cost");
        }
        List<UUID> ids = permanentIds != null ? permanentIds : List.of();
        if (ids.size() != xValue) {
            throw new IllegalStateException("Must remove exactly " + xValue
                    + " counters to cast " + card.getName());
        }

        HashMap<UUID, Integer> selectedCounts = new HashMap<>();
        for (UUID id : ids) {
            if (id == null) {
                throw new IllegalStateException("Invalid permanent selected for the counter cost");
            }
            selectedCounts.merge(id, 1, Integer::sum);
        }
        for (var selected : selectedCounts.entrySet()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, selected.getKey());
            if (permanent == null
                    || !player.getId().equals(gameQueryService.findPermanentController(gameData, selected.getKey()))) {
                throw new IllegalStateException("Counters must be removed from permanents you control");
            }
            if (cost.permanentPredicate() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(permanent, cost.permanentPredicate(),
                    FilterContext.of(gameData)
                            .withSourceControllerId(player.getId()))) {
                throw new IllegalStateException("Permanent does not match the counter cost restriction");
            }
            if (counterCountForVariableCost(permanent, cost.counterType()) < selected.getValue()) {
                throw new IllegalStateException("Permanent does not have enough counters to pay "
                        + card.getName() + "'s cost");
            }
        }
    }

    private int counterCountForVariableCost(Permanent permanent, CounterType counterType) {
        if (counterType == CounterType.ANY) {
            return permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                    + permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        }
        return permanent.getCounterCount(counterType);
    }

    private int counterCount(Permanent permanent, CounterType counterType) {
        return switch (counterType) {
            case PLUS_ONE_PLUS_ONE -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            case MINUS_ONE_MINUS_ONE -> permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
            case CHARGE -> permanent.getCounterCount(CounterType.CHARGE);
            case ANY -> permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                    + permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            default -> 0;
        };
    }

    /**
     * Validates a multi-permanent sacrifice additional cast cost (Phyrexian Tribute's "sacrifice
     * two creatures") without mutating anything. Requires exactly {@code cost.count()} distinct
     * permanents the caster controls, each matching the cost's filter. Returns them in selection
     * order so payment cannot re-resolve a different set.
     */
    public List<Permanent> validateMultipleSacrificeCost(GameData gameData, Player player, Card card,
                                                         SacrificeMultiplePermanentsCost cost,
                                                         List<UUID> sacrificePermanentIds) {
        List<UUID> ids = sacrificePermanentIds != null ? sacrificePermanentIds : List.of();
        if (ids.size() != cost.count()) {
            throw new IllegalStateException("Must sacrifice " + cost.count()
                    + " permanents to cast " + card.getName());
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate sacrifice targets for " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = validateSingleSacrificeCost(gameData, player, card, id, "a matching permanent",
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
            if (gameQueryService.isCreature(gameData, permanent)) {
                validateCanSacrificeCreatureForCost(gameData, card);
            }
            chosen.add(permanent);
        }
        return chosen;
    }

    /** Validates an exact-count permanent sacrifice cost using an arbitrary permanent predicate. */
    public List<Permanent> validateMultipleSacrificeCost(GameData gameData, Player player, Card card,
                                                         int count, String typeDescription,
                                                         Predicate<Permanent> typeCheck,
                                                         List<UUID> sacrificePermanentIds) {
        List<UUID> ids = sacrificePermanentIds != null ? sacrificePermanentIds : List.of();
        if (ids.size() != count) {
            throw new IllegalStateException("Must sacrifice " + typeDescription + " to cast " + card.getName());
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate sacrifice targets for " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            chosen.add(validateSingleSacrificeCost(gameData, player, card, id, typeDescription, typeCheck));
        }
        return chosen;
    }

    /**
     * Validates a sacrifice cost that is paid once for each selected mode beyond the first.
     */
    public List<Permanent> validateEscalateSacrificeCost(GameData gameData, Player player, Card card,
                                                         EscalateSacrificeCost cost, int modesChosen,
                                                         List<UUID> sacrificePermanentIds) {
        int required = cost.count() * Math.max(0, modesChosen - 1);
        List<UUID> ids = sacrificePermanentIds != null ? sacrificePermanentIds : List.of();
        if (ids.size() != required) {
            throw new IllegalStateException("Must sacrifice " + required
                    + " permanents to escalate " + card.getName());
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate escalate sacrifice targets for " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = validateSingleSacrificeCost(gameData, player, card, id, "a matching permanent",
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
            if (gameQueryService.isCreature(gameData, permanent)) {
                validateCanSacrificeCreatureForCost(gameData, card);
            }
            chosen.add(permanent);
        }
        return chosen;
    }

    /** Validates the tap payment required for each selected mode beyond the first. */
    public List<Permanent> validateEscalateTapCost(GameData gameData, Player player, Card card,
                                                    EscalateTapCost cost, int modesChosen,
                                                    List<UUID> tapPermanentIds) {
        int required = Math.max(0, modesChosen - 1);
        List<UUID> ids = tapPermanentIds != null ? tapPermanentIds : List.of();
        if (ids.size() != required) {
            throw new IllegalStateException("Must tap " + required + " permanents to escalate " + card.getName());
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate escalate tap targets for " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            chosen.add(validateSingleTapCost(gameData, player, card, cost.filter(), id));
        }
        return chosen;
    }

    /**
     * Validates the "sacrifice any number of permanents you control" additional cast cost (Devouring
     * Greed) without mutating anything. Any count is legal, including zero, but every chosen
     * permanent must be distinct, controlled by the caster, and match the cost's filter. Returns
     * them in selection order so payment cannot re-resolve a different set.
     */
    public List<Permanent> validateSacrificeAnyNumberOfPermanentsCost(GameData gameData, Player player, Card card,
                                                                      SacrificeAnyNumberOfPermanentsCost cost,
                                                                      List<UUID> sacrificePermanentIds) {
        List<UUID> ids = sacrificePermanentIds != null ? sacrificePermanentIds : List.of();
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate permanents chosen to sacrifice for " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = validateSingleSacrificeCost(gameData, player, card, id, "a matching permanent",
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()));
            if (gameQueryService.isCreature(gameData, permanent)) {
                validateCanSacrificeCreatureForCost(gameData, card);
            }
            chosen.add(permanent);
        }
        return chosen;
    }

    /**
     * Validates the "tap any number of untapped permanents you control" additional cast cost (Burn
     * at the Stake) without mutating anything. Any count is legal, including zero, but every chosen
     * permanent must be distinct, controlled by the caster, untapped, and match the cost's filter.
     * Returns them in selection order so payment cannot re-resolve a different set.
     */
    public List<Permanent> validateTapAnyNumberOfPermanentsCost(GameData gameData, Player player, Card card,
                                                                TapAnyNumberOfPermanentsCost cost,
                                                                List<UUID> tapPermanentIds) {
        List<UUID> ids = tapPermanentIds != null ? tapPermanentIds : List.of();
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate permanents chosen to tap for " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                throw new IllegalStateException("Permanent to tap not found on battlefield");
            }
            if (!player.getId().equals(gameQueryService.findPermanentController(gameData, id))) {
                throw new IllegalStateException("Can only tap permanents you control to cast " + card.getName());
            }
            if (permanent.isTapped()) {
                throw new IllegalStateException("Cannot tap an already tapped permanent to cast " + card.getName());
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, cost.filter())) {
                throw new IllegalStateException("Permanent does not match the tap cost of " + card.getName());
            }
            chosen.add(permanent);
        }
        return chosen;
    }

    /** Validates a spell cost that taps untapped permanents the caster controls. */
    public List<Permanent> validateTapMultiplePermanentsCost(GameData gameData, Player player, Card card,
                                                             TapMultiplePermanentsCost cost,
                                                             List<UUID> tapPermanentIds) {
        return validateTapMultiplePermanentsCost(gameData, player, card, cost, tapPermanentIds, 0);
    }

    public List<Permanent> validateTapMultiplePermanentsCost(GameData gameData, Player player, Card card,
                                                             TapMultiplePermanentsCost cost,
                                                             List<UUID> tapPermanentIds,
                                                             int announcedXValue) {
        List<UUID> ids = tapPermanentIds != null ? tapPermanentIds : List.of();
        int requiredCount = fixedTapCount(cost, announcedXValue);
        if (ids.size() != requiredCount) {
            throw new IllegalStateException("Must tap " + requiredCount + " permanents to cast " + card.getName());
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate permanents chosen to tap for " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                throw new IllegalStateException("Permanent to tap not found on battlefield");
            }
            if (!player.getId().equals(gameQueryService.findPermanentController(gameData, id))) {
                throw new IllegalStateException("Can only tap permanents you control to cast " + card.getName());
            }
            if (permanent.isTapped()) {
                throw new IllegalStateException("Cannot tap an already tapped permanent to cast " + card.getName());
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, cost.filter())) {
                throw new IllegalStateException("Permanent does not match the tap cost of " + card.getName());
            }
            chosen.add(permanent);
        }
        return chosen;
    }

    /** Validates the selected artifacts and creatures paying a spell's Waterbend cost. */
    public List<Permanent> validateWaterbendCost(GameData gameData, Player player, Card card,
                                                  WaterbendCost cost, List<UUID> waterbendPermanentIds) {
        return validateWaterbendCost(gameData, player, card, cost, waterbendPermanentIds, 0);
    }

    /** Validates a Waterbend cost using the spell's announced X value when it scales with X. */
    public List<Permanent> validateWaterbendCost(GameData gameData, Player player, Card card,
                                                  WaterbendCost cost, List<UUID> waterbendPermanentIds,
                                                  int announcedXValue) {
        List<UUID> ids = waterbendPermanentIds != null ? waterbendPermanentIds : List.of();
        int amount = cost.effectiveAmount(announcedXValue);
        if (ids.size() > amount) {
            throw new IllegalStateException("Cannot tap more than " + amount
                    + " permanents for the waterbend cost of " + card.getName());
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate permanents chosen for the waterbend cost of " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                throw new IllegalStateException("Permanent to tap for waterbend not found on battlefield");
            }
            if (!player.getId().equals(gameQueryService.findPermanentController(gameData, id))) {
                throw new IllegalStateException("Can only tap permanents you control for the waterbend cost of "
                        + card.getName());
            }
            if (permanent.isTapped()) {
                throw new IllegalStateException("Cannot tap an already tapped permanent for the waterbend cost of "
                        + card.getName());
            }
            if (!gameQueryService.isArtifact(gameData, permanent)
                    && !gameQueryService.isCreature(gameData, permanent)) {
                throw new IllegalStateException("Waterbend can tap only artifacts or creatures");
            }
            chosen.add(permanent);
        }
        return chosen;
    }

    private int fixedTapCount(TapMultiplePermanentsCost cost) {
        return fixedTapCount(cost, 0);
    }

    private int fixedTapCount(TapMultiplePermanentsCost cost, int announcedXValue) {
        if (cost.count() instanceof Fixed fixed) {
            return fixed.value();
        }
        if (cost.count() instanceof com.github.laxika.magicalvibes.model.amount.XValue) {
            return announcedXValue;
        }
        throw new IllegalStateException("Spell tap costs must use a fixed count or X");
    }

    /**
     * Validates a single "tap an untapped permanent you control" cost (the non-mana splice cost of
     * Hundred-Talon Strike) without mutating anything. The permanent must exist, be controlled by
     * the caster, be untapped, and match {@code filter}. Tapping as a cost is not a {@code {T}}
     * activation cost, so summoning sickness does not matter (CR 302.6 applies to {@code {T}} in
     * activation costs only).
     */
    public Permanent validateSingleTapCost(GameData gameData, Player player, Card card,
                                           PermanentPredicate filter, UUID tapPermanentId) {
        if (tapPermanentId == null) {
            throw new IllegalStateException("A permanent must be chosen to tap for " + card.getName());
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, tapPermanentId);
        if (permanent == null) {
            throw new IllegalStateException("Permanent to tap not found on battlefield");
        }
        if (!player.getId().equals(gameQueryService.findPermanentController(gameData, tapPermanentId))) {
            throw new IllegalStateException("Can only tap permanents you control to cast " + card.getName());
        }
        if (permanent.isTapped()) {
            throw new IllegalStateException("Cannot tap an already tapped permanent to cast " + card.getName());
        }
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
            throw new IllegalStateException("Permanent does not match the tap cost of " + card.getName());
        }
        return permanent;
    }

    /**
     * Validates the "return any number of permanents you control to their owner's hand" additional
     * cast cost (Infernal Harvest) without mutating anything. Any count is legal, including zero,
     * but every chosen permanent must be distinct, controlled by the caster, and match the cost's
     * filter. Returns them in selection order so payment cannot re-resolve a different set.
     */
    public List<Permanent> validateReturnAnyNumberOfPermanentsToHandCost(GameData gameData, Player player, Card card,
                                                                         ReturnAnyNumberOfPermanentsToHandCost cost,
                                                                         List<UUID> returnPermanentIds) {
        List<UUID> ids = returnPermanentIds != null ? returnPermanentIds : List.of();
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalStateException("Duplicate permanents chosen to return for " + card.getName());
        }
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : ids) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                throw new IllegalStateException("Permanent to return not found on battlefield");
            }
            if (!player.getId().equals(gameQueryService.findPermanentController(gameData, id))) {
                throw new IllegalStateException("Can only return permanents you control to cast " + card.getName());
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, cost.filter())) {
                throw new IllegalStateException("Permanent does not match the return cost of " + card.getName());
            }
            chosen.add(permanent);
        }
        return chosen;
    }

    /**
     * Validates the "return a creature you control to its owner's hand" cost (e.g. Familiar's
     * Ruse) without mutating anything. Returns the creature that would be returned.
     */
    public Permanent validateReturnCreatureToHandCost(GameData gameData, Player player, Card card, UUID returnPermanentId) {
        if (returnPermanentId == null) {
            throw new IllegalStateException("Must return a creature you control to cast " + card.getName());
        }
        Permanent toReturn = gameQueryService.findPermanentById(gameData, returnPermanentId);
        if (toReturn == null) {
            throw new IllegalStateException("Return target not found on battlefield");
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, returnPermanentId);
        if (!player.getId().equals(controllerId)) {
            throw new IllegalStateException("Can only return creatures you control");
        }
        if (!gameQueryService.isCreature(gameData, toReturn)) {
            throw new IllegalStateException("Return target must be a creature");
        }
        return toReturn;
    }

    /**
     * Validates the "return a permanent you control to its owner's hand" additional cast cost
     * without mutating anything.
     */
    public Permanent validateReturnPermanentToHandCost(GameData gameData, Player player, Card card,
                                                       ReturnPermanentToHandCost cost, UUID returnPermanentId) {
        if (returnPermanentId == null) {
            throw new IllegalStateException("Must return a permanent you control to cast " + card.getName());
        }
        Permanent toReturn = gameQueryService.findPermanentById(gameData, returnPermanentId);
        if (toReturn == null) {
            throw new IllegalStateException("Return target not found on battlefield");
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, returnPermanentId);
        if (!player.getId().equals(controllerId)) {
            throw new IllegalStateException("Can only return permanents you control");
        }
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, toReturn, cost.filter())) {
            throw new IllegalStateException("Return target does not match the return cost");
        }
        return toReturn;
    }

    /**
     * Validates the "put a counter on a creature you control" cost (e.g. Scarscale Ritual)
     * without mutating anything. Returns the creature that would receive the counter.
     */
    public Permanent validatePutCounterOnControlledCreatureCost(GameData gameData, Player player, Card card,
                                                                PutCounterOnControlledCreatureCost cost, UUID creatureId) {
        return validateControlledCreatureForCounterCost(gameData, player, card, creatureId);
    }

    /** Validates the creature choice for a blight additional cast cost. */
    public Permanent validateBlightCost(GameData gameData, Player player, Card card, UUID creatureId) {
        return validateControlledCreatureForCounterCost(gameData, player, card, creatureId);
    }

    /** Validates the counter-payment option of a counter-or-mana additional cast cost. */
    public Permanent validatePutCountersOnControlledCreatureOrPayManaCost(
            GameData gameData, Player player, Card card,
            PutCountersOnControlledCreatureOrPayManaCost cost, UUID creatureId) {
        return validateControlledCreatureForCounterCost(gameData, player, card, creatureId);
    }

    private Permanent validateControlledCreatureForCounterCost(GameData gameData, Player player, Card card,
                                                               UUID creatureId) {
        if (creatureId == null) {
            throw new IllegalStateException("Must put a counter on a creature you control to cast " + card.getName());
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null) {
            throw new IllegalStateException("Counter target not found on battlefield");
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, creatureId);
        if (!player.getId().equals(controllerId)) {
            throw new IllegalStateException("Can only put a counter on a creature you control");
        }
        if (!gameQueryService.isCreature(gameData, creature)) {
            throw new IllegalStateException("Counter target must be a creature");
        }
        return creature;
    }

    /**
     * Validates the "exile a card from your graveyard" cost without mutating anything. Returns
     * the card that would be exiled.
     */
    public Card validateExileGraveyardCost(GameData gameData, Player player, Card card,
                                           ExileCardFromGraveyardCost cost, Integer exileGraveyardCardIndex) {
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        if (exileGraveyardCardIndex == null) {
            throw new IllegalStateException("Must exile a creature card from your graveyard to cast " + card.getName());
        }
        if (graveyard == null || exileGraveyardCardIndex < 0 || exileGraveyardCardIndex >= graveyard.size()) {
            throw new IllegalStateException("Invalid graveyard card index");
        }
        Card exiledCard = graveyard.get(exileGraveyardCardIndex);
        if (cost.requiredType() != null && !exiledCard.hasType(cost.requiredType())
                && !(cost.alternateType() != null && exiledCard.hasType(cost.alternateType()))) {
            String typeName = cost.requiredType().name().toLowerCase()
                    + (cost.alternateType() != null ? " or " + cost.alternateType().name().toLowerCase() : "");
            throw new IllegalStateException("Must exile a " + typeName + " card from your graveyard");
        }
        return exiledCard;
    }

    /** Validates the "exile X cards from your graveyard" cost without mutating anything. */
    public void validateExileXCardsFromGraveyardCost(GameData gameData, Player player, Card card,
                                                     ExileXCardsFromGraveyardCost cost, List<Integer> exileGraveyardCardIndices) {
        validateExileXCardsFromGraveyardCost(gameData, player, card, cost, exileGraveyardCardIndices, null);
    }

    private void validateExileXCardsFromGraveyardCost(GameData gameData, Player player, Card card,
                                                      ExileXCardsFromGraveyardCost cost,
                                                      List<Integer> exileGraveyardCardIndices,
                                                      Integer announcedXValue) {
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        if (exileGraveyardCardIndices == null) {
            throw new IllegalStateException("Must specify cards to exile from your graveyard to cast " + card.getName());
        }
        if (announcedXValue != null && card.getParsedManaCost() != null && card.getParsedManaCost().hasX()
                && exileGraveyardCardIndices.size() != announcedXValue) {
            throw new IllegalStateException("Must exile exactly " + announcedXValue
                    + " cards from your graveyard to cast " + card.getName());
        }
        if (graveyard == null && !exileGraveyardCardIndices.isEmpty()) {
            throw new IllegalStateException("No cards in graveyard to exile");
        }
        for (int idx : exileGraveyardCardIndices) {
            if (idx < 0 || idx >= graveyard.size()) {
                throw new IllegalStateException("Invalid graveyard card index: " + idx);
            }
            if (cost.requiredType() != null && !graveyard.get(idx).hasType(cost.requiredType())) {
                throw new IllegalStateException("Must exile " + cost.requiredType().name().toLowerCase()
                        + " cards from your graveyard to cast " + card.getName());
            }
        }
    }

    /** Validates the selected cards for a collect-evidence additional cast cost. */
    public void validateCollectEvidenceCost(GameData gameData, Player player, Card card,
                                            CollectEvidenceCost cost,
                                            List<Integer> exileGraveyardCardIndices) {
        validateCollectEvidenceCost(gameData, player, card, cost, exileGraveyardCardIndices,
                cost.minimumManaValue());
    }

    /** Validates evidence against a threshold already fixed during spell announcement. */
    public void validateCollectEvidenceCost(GameData gameData, Player player, Card card,
                                            CollectEvidenceCost cost,
                                            List<Integer> exileGraveyardCardIndices,
                                            int minimumManaValue) {
        List<Integer> indices = exileGraveyardCardIndices == null
                ? List.of() : exileGraveyardCardIndices;
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(player.getId(), List.of());
        if (indices.stream().distinct().count() != indices.size()) {
            throw new IllegalStateException("Duplicate graveyard card indices for " + card.getName());
        }
        int totalManaValue = 0;
        for (int idx : indices) {
            if (idx < 0 || idx >= graveyard.size()) {
                throw new IllegalStateException("Invalid graveyard card index: " + idx);
            }
            totalManaValue += graveyard.get(idx).getManaValue();
        }
        if (totalManaValue < minimumManaValue) {
            throw new IllegalStateException("Must collect evidence " + minimumManaValue
                    + " to cast " + card.getName());
        }
    }

    public int resolveCollectEvidenceMinimumManaValue(GameData gameData, CollectEvidenceCost cost,
                                                      UUID targetId, List<UUID> targetIds) {
        if (cost == null) {
            return 0;
        }
        if (!cost.usesTargetManaValue()) {
            return cost.minimumManaValue();
        }
        List<UUID> allTargetIds = new ArrayList<>();
        if (targetIds != null) {
            allTargetIds.addAll(targetIds);
        }
        if (targetId != null) {
            allTargetIds.add(targetId);
        }
        return allTargetIds.stream()
                .distinct()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .mapToInt(permanent -> permanent.getCard().getManaValue())
                .sum();
    }

    private int requireResolvedCollectEvidenceMinimum(Integer resolvedMinimum, Card card) {
        if (resolvedMinimum == null) {
            throw new IllegalStateException("Target-based evidence threshold was not resolved for " + card.getName());
        }
        return resolvedMinimum;
    }

    /**
     * Validates the "exile exactly N cards from your graveyard" cost without mutating anything.
     * {@code excludedGraveyardIndex} handles graveyard casts (e.g. Skaab Ruinator): the spell
     * itself still sits in the caster's graveyard at validation time but will have been removed
     * by payment time, so the caller's post-removal indices are shifted past it — pass the
     * spell's own graveyard index, or a negative value when the spell is not in this graveyard.
     */
    public void validateExileNCardsFromGraveyardCost(GameData gameData, Player player, Card card,
                                                     ExileNCardsFromGraveyardCost cost, List<Integer> exileGraveyardCardIndices,
                                                     int excludedGraveyardIndex) {
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        int effectiveSize = graveyard == null ? 0 : graveyard.size() - (excludedGraveyardIndex >= 0 ? 1 : 0);
        if (exileGraveyardCardIndices == null || exileGraveyardCardIndices.size() != cost.count()) {
            throw new IllegalStateException("Must exile exactly " + cost.count() + " "
                    + (cost.requiredType() != null ? cost.requiredType().name().toLowerCase() + " " : "")
                    + "cards from your graveyard to cast " + card.getName());
        }
        if (graveyard == null || effectiveSize < cost.count()) {
            throw new IllegalStateException("Not enough cards in graveyard to exile");
        }
        if (exileGraveyardCardIndices.stream().distinct().count() != exileGraveyardCardIndices.size()) {
            throw new IllegalStateException("Duplicate graveyard card indices");
        }
        for (int idx : exileGraveyardCardIndices) {
            if (idx < 0 || idx >= effectiveSize) {
                throw new IllegalStateException("Invalid graveyard card index: " + idx);
            }
            int actualIdx = excludedGraveyardIndex >= 0 && idx >= excludedGraveyardIndex ? idx + 1 : idx;
            Card selected = graveyard.get(actualIdx);
            if ((cost.requiredType() != null && !selected.hasType(cost.requiredType()))
                    || (cost.predicate() != null
                    && !predicateEvaluationService.matchesCardPredicate(selected, cost.predicate(), null))) {
                String typeName = cost.requiredType() != null
                        ? cost.requiredType().name().toLowerCase()
                        : "matching";
                throw new IllegalStateException("Must exile a " + typeName + " card from your graveyard");
            }
        }
    }

    /** Validates the selected cards for a delve cost; the spell's generic mana limit is supplied by the cast path. */
    public void validateDelveCost(GameData gameData, Player player, Card card, DelveCost cost,
                                  List<Integer> exileGraveyardCardIndices) {
        List<Integer> indices = exileGraveyardCardIndices != null ? exileGraveyardCardIndices : List.of();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(player.getId(), List.of());
        if (indices.stream().distinct().count() != indices.size()) {
            throw new IllegalStateException("Duplicate graveyard card indices for " + card.getName());
        }
        for (int idx : indices) {
            if (idx < 0 || idx >= graveyard.size()) {
                throw new IllegalStateException("Invalid graveyard card index: " + idx);
            }
        }
    }

    /** Validates delve after the cast path determines how much generic mana the spell needs. */
    public void validateDelveCost(GameData gameData, Player player, Card card, DelveCost cost,
                                  List<Integer> exileGraveyardCardIndices, int maximumReduction) {
        validateDelveCost(gameData, player, card, cost, exileGraveyardCardIndices);
        int selectedCount = exileGraveyardCardIndices == null ? 0 : exileGraveyardCardIndices.size();
        if (selectedCount > maximumReduction) {
            throw new IllegalStateException("Cannot exile more cards for delve than the spell's generic cost");
        }
    }

    /** Returns the number of graveyard cards selected for delve. */
    public int delveReduction(ExtractedCosts costs, List<Integer> exileGraveyardCardIndices) {
        if (costs.delveCost() == null || exileGraveyardCardIndices == null) {
            return 0;
        }
        return exileGraveyardCardIndices.size();
    }

    /**
     * Validates the "discard a card" cost (e.g. Seize the Spoils) without mutating anything.
     * {@code cost} must be non-null. Returns the index into the current hand (spell already
     * removed) of the card that would be discarded.
     */
    public int validateDiscardCost(GameData gameData, Player player, Card card, DiscardCardTypeCost cost,
                                   Integer discardHandCardIndex, int spellCardIndex) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        String label = cost.label() != null ? cost.label() + " card" : "a card";
        if (discardHandCardIndex == null || discardHandCardIndex == spellCardIndex || hand == null) {
            throw new IllegalStateException("Must discard " + label + " to cast " + card.getName());
        }
        // The spell has already been removed from hand at spellCardIndex, so shift indices past it down.
        int effectiveIndex = spellCardIndex >= 0 && discardHandCardIndex > spellCardIndex
                ? discardHandCardIndex - 1 : discardHandCardIndex;
        if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
            throw new IllegalStateException("Must discard " + label + " to cast " + card.getName());
        }
        Card toDiscard = hand.get(effectiveIndex);
        if (cost.predicate() != null
                && !predicateEvaluationService.matchesCardPredicate(toDiscard, cost.predicate(), toDiscard.getId())) {
            throw new IllegalStateException("Discarded card must be " + label);
        }
        return effectiveIndex;
    }

    /** Validates the revealed hand card for an additional cast cost without moving it. */
    public int validateRevealCardCost(GameData gameData, Player player, Card card,
                                      RevealCardFromHandCost cost, Integer handCardIndex,
                                      int spellCardIndex) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        String label = cost.label() != null ? cost.label() + " card" : "a card";
        if (handCardIndex == null || handCardIndex == spellCardIndex || hand == null) {
            throw new IllegalStateException("Must reveal " + label + " to cast " + card.getName());
        }
        int effectiveIndex = spellCardIndex >= 0 && handCardIndex > spellCardIndex
                ? handCardIndex - 1 : handCardIndex;
        if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
            throw new IllegalStateException("Must reveal " + label + " to cast " + card.getName());
        }
        Card toReveal = hand.get(effectiveIndex);
        if (toReveal.getId().equals(card.getId())
                || (cost.predicate() != null
                && !predicateEvaluationService.matchesCardPredicate(toReveal, cost.predicate(), toReveal.getId()))) {
            throw new IllegalStateException("Revealed card must be " + label);
        }
        return effectiveIndex;
    }

    /** Validates a fixed-count discard additional cast cost without mutating anything. */
    public List<Integer> validateDiscardCardsCost(GameData gameData, Player player, Card card,
                                                   DiscardCardTypeCost cost,
                                                   List<Integer> discardHandCardIndices,
                                                   int spellCardIndex) {
        List<Integer> indices = discardHandCardIndices != null ? discardHandCardIndices : List.of();
        String label = cost.label() != null ? cost.label() + " card" : "card";
        if (indices.size() != cost.count()) {
            throw new IllegalStateException("Must discard " + cost.count() + " " + label
                    + (cost.count() == 1 ? "" : "s") + " to cast " + card.getName());
        }
        if (indices.stream().distinct().count() != indices.size()) {
            throw new IllegalStateException("Duplicate discard indices for " + card.getName());
        }
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (hand == null) {
            throw new IllegalStateException("Must discard " + cost.count() + " " + label
                    + (cost.count() == 1 ? "" : "s") + " to cast " + card.getName());
        }
        List<Integer> effectiveIndices = new ArrayList<>();
        for (int discardHandCardIndex : indices) {
            if (discardHandCardIndex == spellCardIndex) {
                throw new IllegalStateException("Cannot discard the spell itself to pay for " + card.getName());
            }
            int effectiveIndex = spellCardIndex >= 0 && discardHandCardIndex > spellCardIndex
                    ? discardHandCardIndex - 1 : discardHandCardIndex;
            if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
                throw new IllegalStateException("Must discard " + cost.count() + " " + label
                        + (cost.count() == 1 ? "" : "s") + " to cast " + card.getName());
            }
            Card toDiscard = hand.get(effectiveIndex);
            if (cost.predicate() != null
                    && !predicateEvaluationService.matchesCardPredicate(toDiscard, cost.predicate(), toDiscard.getId())) {
                throw new IllegalStateException("Discarded cards must be " + label);
            }
            effectiveIndices.add(effectiveIndex);
        }
        return effectiveIndices;
    }

    /** Validates a random-discard additional cast cost without mutating the hand. */
    public void validateRandomDiscardCost(GameData gameData, Player player, Card card) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (hand == null || hand.stream().noneMatch(candidate -> !candidate.getId().equals(card.getId()))) {
            throw new IllegalStateException("Must discard a card at random to cast " + card.getName());
        }
    }

    /**
     * Validates the "discard X cards" additional cast cost (Abandon Hope) against the announced X,
     * without mutating anything. Returns the post-spell-removal hand indices that would be
     * discarded. Kept out of {@link #validateAll} because only the cast path knows the announced X.
     * When the cost carries a predicate ("discard X land cards" — Scorched Earth), every chosen
     * card must match it.
     */
    public List<Integer> validateDiscardXCardsCost(GameData gameData, Player player, Card card, int announcedX,
                                                   List<Integer> discardHandCardIndices, int spellCardIndex) {
        return validateDiscardXCardsCost(gameData, player, card, peek(card).discardXCardsCost(), announcedX,
                discardHandCardIndices, spellCardIndex);
    }

    /** As {@link #validateDiscardXCardsCost}, for callers that already extracted the cost. */
    public List<Integer> validateDiscardXCardsCost(GameData gameData, Player player, Card card, DiscardXCardsCost cost,
                                                   int announcedX, List<Integer> discardHandCardIndices,
                                                   int spellCardIndex) {
        if (announcedX < 0) {
            throw new IllegalStateException("X cannot be negative for " + card.getName());
        }
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (cost != null && cost.randomSelection()) {
            if (announcedX == 0) {
                return List.of();
            }
            if (hand == null) {
                throw new IllegalStateException("Must discard cards at random to cast " + card.getName());
            }
            long eligibleCount = hand.stream()
                    .filter(candidate -> !candidate.getId().equals(card.getId()))
                    .filter(candidate -> cost.predicate() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            candidate, cost.predicate(), candidate.getId()))
                    .count();
            if (eligibleCount < announcedX) {
                throw new IllegalStateException("Must discard " + announcedX + " card"
                        + (announcedX == 1 ? "" : "s") + " at random to cast " + card.getName());
            }
            return List.of();
        }
        List<Integer> indices = discardHandCardIndices != null ? discardHandCardIndices : List.of();
        if (indices.size() != announcedX) {
            throw new IllegalStateException("Must discard " + announcedX + " card"
                    + (announcedX == 1 ? "" : "s") + " to cast " + card.getName());
        }
        if (announcedX == 0) {
            return List.of();
        }
        if (indices.stream().distinct().count() != indices.size()) {
            throw new IllegalStateException("Duplicate discard indices for " + card.getName());
        }
        if (hand == null) {
            throw new IllegalStateException("Must discard cards to cast " + card.getName());
        }
        List<Integer> effectiveIndices = new ArrayList<>();
        for (int discardHandCardIndex : indices) {
            if (discardHandCardIndex == spellCardIndex) {
                throw new IllegalStateException("Cannot discard " + card.getName() + " to pay for itself");
            }
            int effectiveIndex = spellCardIndex >= 0 && discardHandCardIndex > spellCardIndex
                    ? discardHandCardIndex - 1 : discardHandCardIndex;
            if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
                throw new IllegalStateException("Must discard cards to cast " + card.getName());
            }
            Card toDiscard = hand.get(effectiveIndex);
            if (cost != null && cost.predicate() != null
                    && !predicateEvaluationService.matchesCardPredicate(toDiscard, cost.predicate(), toDiscard.getId())) {
                throw new IllegalStateException("Discarded cards must be " + cost.label());
            }
            effectiveIndices.add(effectiveIndex);
        }
        return effectiveIndices;
    }

    /**
     * Validates escalate's "discard a card for each mode beyond the first" cost without mutating.
     * Returns the post-spell-removal hand indices that would be discarded (descending order ready
     * for payment). {@code modesChosen} must be &gt;= 1.
     */
    public List<Integer> validateEscalateDiscardCost(GameData gameData, Player player, Card card,
                                                     int modesChosen, List<Integer> discardHandCardIndices,
                                                     int spellCardIndex) {
        int required = Math.max(0, modesChosen - 1);
        if (required == 0) {
            if (discardHandCardIndices != null && !discardHandCardIndices.isEmpty()) {
                throw new IllegalStateException("No escalate discard required for a single mode of " + card.getName());
            }
            return List.of();
        }
        if (discardHandCardIndices == null || discardHandCardIndices.size() != required) {
            throw new IllegalStateException("Must discard " + required + " card"
                    + (required == 1 ? "" : "s") + " to escalate " + card.getName());
        }
        if (discardHandCardIndices.stream().distinct().count() != discardHandCardIndices.size()) {
            throw new IllegalStateException("Duplicate escalate discard indices");
        }
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (hand == null) {
            throw new IllegalStateException("Must discard cards to escalate " + card.getName());
        }
        List<Integer> effectiveIndices = new ArrayList<>();
        for (int discardHandCardIndex : discardHandCardIndices) {
            if (discardHandCardIndex == spellCardIndex) {
                throw new IllegalStateException("Cannot discard the spell itself to escalate " + card.getName());
            }
            int effectiveIndex = spellCardIndex >= 0 && discardHandCardIndex > spellCardIndex
                    ? discardHandCardIndex - 1 : discardHandCardIndex;
            if (effectiveIndex < 0 || effectiveIndex >= hand.size()) {
                throw new IllegalStateException("Must discard cards to escalate " + card.getName());
            }
            effectiveIndices.add(effectiveIndex);
        }
        if (effectiveIndices.stream().distinct().count() != effectiveIndices.size()) {
            throw new IllegalStateException("Duplicate escalate discard indices");
        }
        return effectiveIndices;
    }

    /**
     * Counts how many modes a modal encoding selects for an escalate payment. Returns 0 when the
     * card has no {@link ChooseOneEffect} (caller should not have an escalate cost in that case).
     */
    public int countChosenModes(Card card, int modeEncoding) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .map(coe -> coe.decodeModeIndices(modeEncoding).size())
                .orElse(0);
    }

    /**
     * Validates retrace's additional cost (CR 702.81, discard a land card) without mutating
     * anything. {@code discardHandCardIndex} indexes directly into the caster's hand — the
     * retraced spell is in the graveyard, so no index adjustment applies.
     */
    public void validateRetraceDiscardCost(GameData gameData, Player player, Card card, Integer discardHandCardIndex) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (discardHandCardIndex == null || hand == null
                || discardHandCardIndex < 0 || discardHandCardIndex >= hand.size()) {
            throw new IllegalStateException("Must discard a land card to retrace " + card.getName());
        }
        if (!hand.get(discardHandCardIndex).hasType(CardType.LAND)) {
            throw new IllegalStateException("Must discard a land card to retrace " + card.getName());
        }
    }
}
