package com.github.laxika.magicalvibes.service.effect.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
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
            SacrificeCreatureCost.class,
            SacrificeArtifactCost.class,
            SacrificePermanentCost.class,
            ReturnCreatureToHandCost.class,
            PutCounterOnControlledCreatureCost.class,
            ExileCardFromGraveyardCost.class,
            ExileXCardsFromGraveyardCost.class,
            ExileNCardsFromGraveyardCost.class,
            DiscardCardTypeCost.class);

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * The additional cast costs found on one spell, in canonical payment order. Extracted once
     * per cast so extraction, validation and payment can never disagree about what the spell
     * costs. {@code SpellCastingService.payAdditionalCosts} must consume every field.
     */
    public record ExtractedCosts(
            boolean sacrificeAllCreatures,
            boolean sacrificeCreature,
            boolean sacrificeArtifact,
            SacrificePermanentCost sacrificePermanentCost,
            boolean returnCreatureToHand,
            PutCounterOnControlledCreatureCost putCounterCost,
            ExileCardFromGraveyardCost exileGraveyardCost,
            ExileXCardsFromGraveyardCost exileXCardsCost,
            ExileNCardsFromGraveyardCost exileNCardsCost,
            DiscardCardTypeCost discardCost
    ) {
        /** True when the spell has any additional cast cost at all. */
        public boolean any() {
            return sacrificeAllCreatures || sacrificeCreature || sacrificeArtifact
                    || sacrificePermanentCost != null || returnCreatureToHand || putCounterCost != null
                    || exileGraveyardCost != null || exileXCardsCost != null || exileNCardsCost != null
                    || discardCost != null;
        }
    }

    /**
     * The caster's payment choices, as carried by the cast request. {@code spellCardIndex} is the
     * spell's own pre-removal hand index (used to adjust {@code discardHandCardIndex}); pass a
     * negative value for casts not from hand.
     */
    public record CostSelection(
            UUID sacrificePermanentId,
            Integer exileGraveyardCardIndex,
            List<Integer> exileGraveyardCardIndices,
            Integer discardHandCardIndex,
            int spellCardIndex
    ) {
        public static CostSelection none() {
            return new CostSelection(null, null, null, null, -1);
        }
    }

    /**
     * Removes every handled additional-cost effect from {@code effects} (a mutable copy of the
     * spell's SPELL-slot effects — never the frozen card list itself) and returns them. The
     * stripped list is what goes onto the stack; costs are paid at cast time, not resolved.
     */
    public ExtractedCosts extractAndRemove(List<CardEffect> effects) {
        boolean sacAllCreatures = effects.removeIf(SacrificeAllCreaturesYouControlCost.class::isInstance);
        boolean sacCreature = effects.removeIf(SacrificeCreatureCost.class::isInstance);
        boolean sacArtifact = effects.removeIf(SacrificeArtifactCost.class::isInstance);
        SacrificePermanentCost permCost = removeFirst(effects, SacrificePermanentCost.class);
        boolean returnCreature = effects.removeIf(ReturnCreatureToHandCost.class::isInstance);
        PutCounterOnControlledCreatureCost putCounterCost = removeFirst(effects, PutCounterOnControlledCreatureCost.class);
        ExileCardFromGraveyardCost exileGraveyardCost = removeFirst(effects, ExileCardFromGraveyardCost.class);
        ExileXCardsFromGraveyardCost exileXCardsCost = removeFirst(effects, ExileXCardsFromGraveyardCost.class);
        ExileNCardsFromGraveyardCost exileNCardsCost = removeFirst(effects, ExileNCardsFromGraveyardCost.class);
        DiscardCardTypeCost discardCost = removeFirst(effects, DiscardCardTypeCost.class);
        return new ExtractedCosts(sacAllCreatures, sacCreature, sacArtifact, permCost, returnCreature,
                putCounterCost, exileGraveyardCost, exileXCardsCost, exileNCardsCost, discardCost);
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
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            switch (effect) {
                case SacrificeCreatureCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case ReturnCreatureToHandCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case PutCounterOnControlledCreatureCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case SacrificeArtifactCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isArtifact(gameData, p))) return false;
                }
                case SacrificePermanentCost cost -> {
                    if (battlefield.stream().noneMatch(p ->
                            predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))) return false;
                }
                case ExileNCardsFromGraveyardCost cost -> {
                    long matchingCount = graveyard.stream()
                            .filter(c -> cost.requiredType() == null || c.hasType(cost.requiredType()))
                            .count();
                    if (matchingCount < cost.count()) return false;
                }
                case ExileCardFromGraveyardCost cost -> {
                    if (graveyard.stream().noneMatch(c ->
                            (cost.requiredType() == null || c.hasType(cost.requiredType()))
                                    && (cost.requiredSubtype() == null || c.getSubtypes().contains(cost.requiredSubtype())))) return false;
                }
                case ExileXCardsFromGraveyardCost ignored -> {
                    if (graveyard.isEmpty()) return false;
                }
                case DiscardCardTypeCost cost -> {
                    if (discardCostIndices(gameData, playerId, card, cost).isEmpty()) return false;
                }
                // Sacrificing all creatures you control is legal with zero creatures.
                case SacrificeAllCreaturesYouControlCost ignored -> { }
                default -> { }
            }
        }
        return true;
    }

    /**
     * Hand indices (as the caller/UI sees them, with the spell still in hand) whose card can pay
     * the spell's "discard a card" additional cast cost. Returns {@code null} when the card has no
     * such cost, an empty list when the cost exists but is unpayable. Any card other than the
     * spell itself that matches the cost's predicate qualifies (CR 601.2b — the spell is on the
     * stack when costs are paid, so it can never be its own discard).
     */
    public List<Integer> validDiscardCostIndices(GameData gameData, UUID playerId, Card card) {
        DiscardCardTypeCost cost = peek(card).discardCost();
        return cost == null ? null : discardCostIndices(gameData, playerId, card, cost);
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
        if (costs.sacrificeCreature()) {
            validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    "a creature", p -> gameQueryService.isCreature(gameData, p));
        }
        if (costs.sacrificeArtifact()) {
            validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    "an artifact", p -> gameQueryService.isArtifact(p));
        }
        if (costs.sacrificePermanentCost() != null) {
            validateSingleSacrificeCost(gameData, player, card, selection.sacrificePermanentId(),
                    costs.sacrificePermanentCost().description(),
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, costs.sacrificePermanentCost().filter()));
        }
        // Sacrificing all creatures you control has no failure mode (zero creatures is legal).
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
        if (cost.requiredType() != null && !exiledCard.hasType(cost.requiredType())) {
            String typeName = cost.requiredType().name().toLowerCase();
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
