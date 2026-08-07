package com.github.laxika.magicalvibes.service.effect.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.EscalateDiscardCost;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ReturnAnyNumberOfPermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllPermanentsYouControlCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
            SacrificePermanentCost.class,
            SacrificeMultiplePermanentsCost.class,
            SacrificeAnyNumberOfPermanentsCost.class,
            TapAnyNumberOfPermanentsCost.class,
            ReturnAnyNumberOfPermanentsToHandCost.class,
            ReturnCreatureToHandCost.class,
            PutCounterOnControlledCreatureCost.class,
            PayXLifeCost.class,
            PayLifeCost.class,
            ExileCardFromGraveyardCost.class,
            ExileXCardsFromGraveyardCost.class,
            ExileNCardsFromGraveyardCost.class,
            DiscardCardTypeCost.class,
            DiscardCardOrPayManaCost.class,
            DiscardHandCost.class,
            DiscardXCardsCost.class,
            EscalateDiscardCost.class,
            EscalateManaCost.class,
            RepeatableAdditionalManaCost.class);

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
            SacrificeCreatureOrPayManaCost sacrificeCreatureOrPayManaCost,
            SacrificePermanentCost sacrificePermanentCost,
            SacrificeMultiplePermanentsCost sacrificeMultiplePermanentsCost,
            SacrificeAnyNumberOfPermanentsCost sacrificeAnyNumberCost,
            TapAnyNumberOfPermanentsCost tapAnyNumberCost,
            ReturnAnyNumberOfPermanentsToHandCost returnAnyNumberCost,
            boolean returnCreatureToHand,
            PutCounterOnControlledCreatureCost putCounterCost,
            boolean payXLife,
            PayLifeCost payLifeCost,
            ExileCardFromGraveyardCost exileGraveyardCost,
            ExileXCardsFromGraveyardCost exileXCardsCost,
            ExileNCardsFromGraveyardCost exileNCardsCost,
            DiscardCardTypeCost discardCost,
            DiscardCardOrPayManaCost discardCardOrPayManaCost,
            boolean discardHand,
            DiscardXCardsCost discardXCardsCost,
            EscalateDiscardCost escalateDiscardCost,
            EscalateManaCost escalateManaCost,
            RepeatableAdditionalManaCost repeatableManaCost
    ) {
        /** True when the spell has any additional cast cost at all. */
        public boolean any() {
            return sacrificeAllCreatures || sacrificeAllPermanents || sacrificeCreature
                    || sacrificeCreatureOrPayManaCost != null
                    || sacrificePermanentCost != null || sacrificeMultiplePermanentsCost != null
                    || sacrificeAnyNumberCost != null
                    || tapAnyNumberCost != null || returnAnyNumberCost != null
                    || returnCreatureToHand || putCounterCost != null
                    || payXLife || payLifeCost != null
                    || exileGraveyardCost != null || exileXCardsCost != null || exileNCardsCost != null
                    || discardCost != null || discardCardOrPayManaCost != null || discardHand || discardXCardsCost != null
                    || escalateDiscardCost != null || escalateManaCost != null
                    || repeatableManaCost != null;
        }

        /** True when the spell has any escalate cost (mana and/or discard). */
        public boolean hasEscalate() {
            return escalateDiscardCost != null || escalateManaCost != null;
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
     * sacrifice (Phyrexian Tribute's "sacrifice two creatures"), a "tap any number of permanents
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
            List<UUID> sacrificePermanentIds
    ) {
        public static CostSelection none() {
            return new CostSelection(null, null, null, null, null, 0, -1, List.of());
        }

        /** Convenience for the common single-discard / no-escalate case. */
        public CostSelection(UUID sacrificePermanentId, Integer exileGraveyardCardIndex,
                             List<Integer> exileGraveyardCardIndices, Integer discardHandCardIndex,
                             int spellCardIndex) {
            this(sacrificePermanentId, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    discardHandCardIndex, null, 0, spellCardIndex, List.of());
        }

        public CostSelection(UUID sacrificePermanentId, Integer exileGraveyardCardIndex,
                             List<Integer> exileGraveyardCardIndices, Integer discardHandCardIndex,
                             List<Integer> discardHandCardIndices, int escalateModeCount, int spellCardIndex) {
            this(sacrificePermanentId, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    discardHandCardIndex, discardHandCardIndices, escalateModeCount, spellCardIndex, List.of());
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
        SacrificeCreatureOrPayManaCost sacOrPay = removeFirst(effects, SacrificeCreatureOrPayManaCost.class);
        SacrificePermanentCost permCost = removeFirst(effects, SacrificePermanentCost.class);
        SacrificeMultiplePermanentsCost multiPermCost = removeFirst(effects, SacrificeMultiplePermanentsCost.class);
        SacrificeAnyNumberOfPermanentsCost sacAnyNumberCost =
                removeFirst(effects, SacrificeAnyNumberOfPermanentsCost.class);
        TapAnyNumberOfPermanentsCost tapAnyNumberCost = removeFirst(effects, TapAnyNumberOfPermanentsCost.class);
        ReturnAnyNumberOfPermanentsToHandCost returnAnyNumberCost =
                removeFirst(effects, ReturnAnyNumberOfPermanentsToHandCost.class);
        boolean returnCreature = effects.removeIf(ReturnCreatureToHandCost.class::isInstance);
        PutCounterOnControlledCreatureCost putCounterCost = removeFirst(effects, PutCounterOnControlledCreatureCost.class);
        boolean payXLife = effects.removeIf(PayXLifeCost.class::isInstance);
        PayLifeCost payLifeCost = removeFirst(effects, PayLifeCost.class);
        ExileCardFromGraveyardCost exileGraveyardCost = removeFirst(effects, ExileCardFromGraveyardCost.class);
        ExileXCardsFromGraveyardCost exileXCardsCost = removeFirst(effects, ExileXCardsFromGraveyardCost.class);
        ExileNCardsFromGraveyardCost exileNCardsCost = removeFirst(effects, ExileNCardsFromGraveyardCost.class);
        DiscardCardTypeCost discardCost = removeFirst(effects, DiscardCardTypeCost.class);
        DiscardCardOrPayManaCost discardOrPay = removeFirst(effects, DiscardCardOrPayManaCost.class);
        boolean discardHand = effects.removeIf(DiscardHandCost.class::isInstance);
        DiscardXCardsCost discardXCards = removeFirst(effects, DiscardXCardsCost.class);
        EscalateDiscardCost escalateDiscardCost = removeFirst(effects, EscalateDiscardCost.class);
        EscalateManaCost escalateManaCost = removeFirst(effects, EscalateManaCost.class);
        RepeatableAdditionalManaCost repeatableManaCost = removeFirst(effects, RepeatableAdditionalManaCost.class);
        return new ExtractedCosts(sacAllCreatures, sacAllPermanents, sacCreature, sacOrPay, permCost, multiPermCost,
                sacAnyNumberCost, tapAnyNumberCost, returnAnyNumberCost, returnCreature,
                putCounterCost, payXLife, payLifeCost, exileGraveyardCost, exileXCardsCost, exileNCardsCost, discardCost, discardOrPay,
                discardHand, discardXCards, escalateDiscardCost, escalateManaCost, repeatableManaCost);
    }

    /** Reads the card's additional cast costs without touching the card (for gating queries). */
    public ExtractedCosts peek(Card card) {
        return extractAndRemove(new ArrayList<>(card.getEffects(EffectSlot.SPELL)));
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
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        // Angel of Jubilation: life payments and creature sacrifices are unavailable as cast costs.
        boolean lifeAndSacAllowed = gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData);
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            switch (effect) {
                case SacrificeCreatureCost ignored -> {
                    if (!lifeAndSacAllowed) return false;
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case SacrificeCreatureOrPayManaCost cost -> {
                    boolean hasCreature = lifeAndSacAllowed
                            && battlefield.stream().anyMatch(p -> gameQueryService.isCreature(gameData, p));
                    if (!hasCreature && !canAffordSacrificeOrPayManaOption(gameData, playerId, card, cost)) {
                        return false;
                    }
                }
                case DiscardCardOrPayManaCost cost -> {
                    boolean hasDiscard = !discardCostIndices(gameData, playerId, card,
                            new DiscardCardTypeCost(null, null)).isEmpty();
                    if (!hasDiscard && !canAffordDiscardOrPayManaOption(gameData, playerId, card, cost)) {
                        return false;
                    }
                }
                case ReturnCreatureToHandCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case PutCounterOnControlledCreatureCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
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
                case ExileNCardsFromGraveyardCost cost -> {
                    long matchingCount = graveyard.stream()
                            .filter(c -> cost.requiredType() == null || c.hasType(cost.requiredType()))
                            .count();
                    if (matchingCount < cost.count()) return false;
                }
                case ExileCardFromGraveyardCost cost -> {
                    if (graveyard.stream().noneMatch(c ->
                            (cost.requiredType() == null || c.hasType(cost.requiredType())
                                    || (cost.alternateType() != null && c.hasType(cost.alternateType())))
                                    && (cost.requiredSubtype() == null || c.getSubtypes().contains(cost.requiredSubtype())))) return false;
                }
                case ExileXCardsFromGraveyardCost ignored -> {
                    if (graveyard.isEmpty()) return false;
                }
                case DiscardCardTypeCost cost -> {
                    if (discardCostIndices(gameData, playerId, card, cost).isEmpty()) return false;
                }
                // Paying X life is always payable — X may be announced as 0.
                case PayXLifeCost ignored -> { }
                // A fixed life payment is only legal while the life total covers it (CR 119.4).
                case PayLifeCost cost -> {
                    if (!lifeAndSacAllowed) return false;
                    int life = gameData.getLife(playerId);
                    if (life < cost.effectiveAmount(life)) return false;
                }
                // Escalate is payable with a single mode (zero extra payments), so it never blocks
                // playability by itself — concrete mode+payment selections are validated at cast.
                case EscalateDiscardCost ignored -> { }
                case EscalateManaCost ignored -> { }
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
        return null;
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
        if (costs.payLifeCost() != null) {
            validatePayLifeCost(gameData, player, card, costs.payLifeCost());
        }
        if (costs.sacrificeCreature()) {
            validateCanSacrificeCreatureForCost(gameData, card);
            validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    "a creature", p -> gameQueryService.isCreature(gameData, p));
        }
        if (costs.sacrificeCreatureOrPayManaCost() != null) {
            if (selection.sacrificePermanentId() != null) {
                validateCanSacrificeCreatureForCost(gameData, card);
                validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                        "a creature", p -> gameQueryService.isCreature(gameData, p));
            } else if (!canAffordSacrificeOrPayManaOption(gameData, player.getId(), card,
                    costs.sacrificeCreatureOrPayManaCost())) {
                throw new IllegalStateException("Must sacrifice a creature or pay "
                        + costs.sacrificeCreatureOrPayManaCost().manaCost()
                        + " to cast " + card.getName());
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
        if (costs.sacrificePermanentCost() != null) {
            validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    costs.sacrificePermanentCost().description(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, costs.sacrificePermanentCost().filter()));
        }
        if (costs.sacrificeMultiplePermanentsCost() != null) {
            validateMultipleSacrificeCost(gameData, player, card, costs.sacrificeMultiplePermanentsCost(),
                    selection.sacrificePermanentIds());
        }
        if (costs.sacrificeAnyNumberCost() != null) {
            validateSacrificeAnyNumberOfPermanentsCost(gameData, player, card, costs.sacrificeAnyNumberCost(),
                    selection.sacrificePermanentIds());
        }
        if (costs.tapAnyNumberCost() != null) {
            validateTapAnyNumberOfPermanentsCost(gameData, player, card, costs.tapAnyNumberCost(),
                    selection.sacrificePermanentIds());
        }
        if (costs.returnAnyNumberCost() != null) {
            validateReturnAnyNumberOfPermanentsToHandCost(gameData, player, card, costs.returnAnyNumberCost(),
                    selection.sacrificePermanentIds());
        }
        // Sacrificing all creatures / permanents you control has no failure mode (zero is legal).
        // Discarding your entire hand has no failure mode (empty hand is legal).
        if (costs.returnCreatureToHand()) {
            validateReturnCreatureToHandCost(gameData, player, card, selection.sacrificePermanentId());
        }
        if (costs.putCounterCost() != null) {
            validatePutCounterOnControlledCreatureCost(gameData, player, card, costs.putCounterCost(),
                    selection.sacrificePermanentId());
        }
        if (costs.exileGraveyardCost() != null) {
            validateExileGraveyardCost(gameData, player, card, costs.exileGraveyardCost(),
                    selection.exileGraveyardCardIndex());
        }
        if (costs.exileXCardsCost() != null) {
            validateExileXCardsFromGraveyardCost(gameData, player, card, costs.exileXCardsCost(),
                    selection.exileGraveyardCardIndices());
        }
        if (costs.exileNCardsCost() != null) {
            validateExileNCardsFromGraveyardCost(gameData, player, card, costs.exileNCardsCost(),
                    selection.exileGraveyardCardIndices(), -1);
        }
        if (costs.discardCost() != null) {
            validateDiscardCost(gameData, player, card, costs.discardCost(),
                    selection.discardHandCardIndex(), selection.spellCardIndex());
        }
        if (costs.escalateDiscardCost() != null) {
            validateEscalateDiscardCost(gameData, player, card, selection.escalateModeCount(),
                    selection.discardHandCardIndices(), selection.spellCardIndex());
        }
        if (costs.escalateManaCost() != null) {
            validateEscalateManaCost(card, costs.escalateManaCost(), selection.escalateModeCount());
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
        for (String payment : payments) {
            if (!cost.manaCosts().contains(payment)) {
                throw new IllegalStateException("Invalid additional cost payment " + payment
                        + " for " + card.getName());
            }
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
                                                     SacrificeCreatureOrPayManaCost cost) {
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
            chosen.add(validateSingleSacrificeCost(gameData, player, card, id, "a matching permanent",
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter())));
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
            chosen.add(validateSingleSacrificeCost(gameData, player, card, id, "a matching permanent",
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter())));
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
     * Validates the "put a counter on a creature you control" cost (e.g. Scarscale Ritual)
     * without mutating anything. Returns the creature that would receive the counter.
     */
    public Permanent validatePutCounterOnControlledCreatureCost(GameData gameData, Player player, Card card,
                                                                PutCounterOnControlledCreatureCost cost, UUID creatureId) {
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
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        if (exileGraveyardCardIndices == null) {
            throw new IllegalStateException("Must specify cards to exile from your graveyard to cast " + card.getName());
        }
        if (graveyard == null && !exileGraveyardCardIndices.isEmpty()) {
            throw new IllegalStateException("No cards in graveyard to exile");
        }
        for (int idx : exileGraveyardCardIndices) {
            if (idx < 0 || idx >= graveyard.size()) {
                throw new IllegalStateException("Invalid graveyard card index: " + idx);
            }
        }
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
            if (cost.requiredType() != null && !graveyard.get(actualIdx).hasType(cost.requiredType())) {
                String typeName = cost.requiredType().name().toLowerCase();
                throw new IllegalStateException("Must exile a " + typeName + " card from your graveyard");
            }
        }
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
        List<Card> hand = gameData.playerHands.get(player.getId());
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
