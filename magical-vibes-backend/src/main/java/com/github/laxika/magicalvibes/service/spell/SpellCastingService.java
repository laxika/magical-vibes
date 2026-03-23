package com.github.laxika.magicalvibes.service.spell;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardsAndSeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpellCastingService {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;
    private final TargetLegalityService targetLegalityService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    // --- Helper records ---

    private record SacrificeCostFlags(
            boolean usesSacrificeAllCreaturesCost,
            boolean usesSacrificeCreatureCost,
            boolean usesSacrificeArtifactCost,
            SacrificePermanentCost sacrificePermanentCost
    ) {}

    private record ManaRestrictionFlags(boolean isArtifact, boolean isMyr, boolean hasRestrictedRedContext, boolean kickedOnlyGreen, boolean instantSorceryOnlyColorless) {
        boolean hasRestricted() { return isArtifact || isMyr || hasRestrictedRedContext || kickedOnlyGreen || instantSorceryOnlyColorless; }
    }

    // --- Helper methods ---

    private SacrificeCostFlags extractAndRemoveSacrificeCosts(List<CardEffect> effects) {
        boolean sacAllCreatures = effects.removeIf(SacrificeAllCreaturesYouControlCost.class::isInstance);
        boolean sacCreature = effects.removeIf(SacrificeCreatureCost.class::isInstance);
        boolean sacArtifact = effects.removeIf(SacrificeArtifactCost.class::isInstance);
        SacrificePermanentCost permCost = (SacrificePermanentCost) effects.stream()
                .filter(SacrificePermanentCost.class::isInstance)
                .findFirst().orElse(null);
        if (permCost != null) effects.removeIf(SacrificePermanentCost.class::isInstance);
        return new SacrificeCostFlags(sacAllCreatures, sacCreature, sacArtifact, permCost);
    }

    private ExileCardFromGraveyardCost extractAndRemoveExileGraveyardCost(List<CardEffect> effects) {
        ExileCardFromGraveyardCost cost = (ExileCardFromGraveyardCost) effects.stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .findFirst().orElse(null);
        if (cost != null) effects.removeIf(ExileCardFromGraveyardCost.class::isInstance);
        return cost;
    }

    private ExileXCardsFromGraveyardCost extractAndRemoveExileXCardsFromGraveyardCost(List<CardEffect> effects) {
        ExileXCardsFromGraveyardCost cost = (ExileXCardsFromGraveyardCost) effects.stream()
                .filter(ExileXCardsFromGraveyardCost.class::isInstance)
                .findFirst().orElse(null);
        if (cost != null) effects.removeIf(ExileXCardsFromGraveyardCost.class::isInstance);
        return cost;
    }

    private ExileNCardsFromGraveyardCost extractAndRemoveExileNCardsFromGraveyardCost(List<CardEffect> effects) {
        ExileNCardsFromGraveyardCost cost = (ExileNCardsFromGraveyardCost) effects.stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .findFirst().orElse(null);
        if (cost != null) effects.removeIf(ExileNCardsFromGraveyardCost.class::isInstance);
        return cost;
    }

    private int unwrapChooseOneEffect(Card card, List<CardEffect> effects, int effectiveXValue) {
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) instanceof ChooseOneEffect coe) {
                if (effectiveXValue < 0 || effectiveXValue >= coe.options().size()) {
                    throw new IllegalStateException("Invalid mode index: " + effectiveXValue);
                }
                ChooseOneEffect.ChooseOneOption chosen = coe.options().get(effectiveXValue);
                effects.set(i, chosen.effect());
                // Apply per-mode target filter so downstream validation uses the correct filter
                if (chosen.targetFilter() != null) {
                    card.setCastTimeTargetFilter(chosen.targetFilter());
                }
                return 0;
            }
        }
        return effectiveXValue;
    }

    private ManaRestrictionFlags computeManaRestrictionFlags(GameData gameData, UUID playerId, Card card) {
        return computeManaRestrictionFlags(gameData, playerId, card, false);
    }

    private ManaRestrictionFlags computeManaRestrictionFlags(GameData gameData, UUID playerId, Card card, boolean kicked) {
        boolean isArtifact = card.hasType(CardType.ARTIFACT);
        boolean isMyr = gameQueryService.cardHasSubtype(card, CardSubtype.MYR, gameData, playerId);
        boolean hasRestrictedRedContext = isArtifact || card.hasType(CardType.CREATURE);
        boolean instantSorceryOnlyColorless = card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY);
        return new ManaRestrictionFlags(isArtifact, isMyr, hasRestrictedRedContext, kicked, instantSorceryOnlyColorless);
    }

    private StackEntryType cardTypeToStackEntryType(CardType type) {
        return switch (type) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            default -> throw new IllegalStateException("Unsupported card type: " + type);
        };
    }

    // --- Main methods ---

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, null, null, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, null, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, false);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex,
                  List<Integer> exileGraveyardCardIndices, boolean kicked) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (targetIds == null) targetIds = List.of();
        if (convokeCreatureIds == null) convokeCreatureIds = List.of();
        if (alternateCostSacrificePermanentIds == null) alternateCostSacrificePermanentIds = List.of();
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();

        List<Card> handEarly = gameData.playerHands.get(playerId);
        if (!fromGraveyard && (cardIndex < 0 || cardIndex >= handEarly.size())) {
            throw new IllegalArgumentException("Invalid card index");
        }

        boolean hasSacrificeForCostReduction = !alternateCostSacrificePermanentIds.isEmpty() && !fromGraveyard
                && handEarly.get(cardIndex).getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(SacrificeCreaturesForCostReductionEffect.class::isInstance);
        boolean usingAlternateCost = !alternateCostSacrificePermanentIds.isEmpty() && !hasSacrificeForCostReduction;

        // Handle playing a land from graveyard (e.g. via Crucible of Worlds)
        if (fromGraveyard) {
            List<Integer> playableGraveyard = gameBroadcastService.getPlayableGraveyardLandIndices(gameData, playerId);
            if (!playableGraveyard.contains(cardIndex)) {
                throw new IllegalStateException("Card is not playable from graveyard");
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            Card graveyardCard = graveyard.get(cardIndex);
            if (!graveyardCard.hasType(CardType.LAND)) {
                throw new IllegalStateException("Only lands can be played from graveyard");
            }
            graveyard.remove(cardIndex);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, new Permanent(graveyardCard));
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);

            String logEntry = player.getUsername() + " plays " + graveyardCard.getName() + " from graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} plays {} from graveyard", gameData.id, player.getUsername(), graveyardCard.getName());

            // Process ETB effects for lands (e.g. Glimmerpost)
            battlefieldEntryService.processCreatureETBEffects(gameData, playerId, graveyardCard, null, false);

            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        List<Integer> playable = gameBroadcastService.getPlayableCardIndices(gameData, playerId);
        if (!playable.contains(cardIndex)) {
            // Re-check with convoke if card has convoke keyword
            List<Card> handCheck = gameData.playerHands.get(playerId);
            Card cardCheck = handCheck.get(cardIndex);
            if (usingAlternateCost && cardCheck.getCastingOption(AlternateHandCast.class).isPresent()) {
                // Allow — alternate cost bypasses mana check; validated below
            } else if (cardCheck.getKeywords().contains(Keyword.CONVOKE) && !convokeCreatureIds.isEmpty()) {
                // Allow convoke-assisted casting even if not in basic playable list
                List<Integer> convokePlayable = gameBroadcastService.getPlayableCardIndices(gameData, playerId, convokeCreatureIds.size());
                if (!convokePlayable.contains(cardIndex)) {
                    throw new IllegalStateException("Card is not playable even with convoke");
                }
            } else if (hasSacrificeForCostReduction) {
                // Allow — sacrifice cost reduction will be validated during casting
            } else {
                throw new IllegalStateException("Card is not playable");
            }
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.get(cardIndex);
        List<CardEffect> filteredSpellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        SacrificeCostFlags sacFlags = extractAndRemoveSacrificeCosts(filteredSpellEffects);
        ExileCardFromGraveyardCost exileGraveyardCost = extractAndRemoveExileGraveyardCost(filteredSpellEffects);
        ExileXCardsFromGraveyardCost exileXCardsGraveyardCost = extractAndRemoveExileXCardsFromGraveyardCost(filteredSpellEffects);
        ExileNCardsFromGraveyardCost exileNCardsGraveyardCost = extractAndRemoveExileNCardsFromGraveyardCost(filteredSpellEffects);

        // Handle modal spells (Choose one): unwrap at cast time per MTG CR 700.2a
        boolean wasModal = filteredSpellEffects.stream().anyMatch(ChooseOneEffect.class::isInstance);
        effectiveXValue = unwrapChooseOneEffect(card, filteredSpellEffects, effectiveXValue);

        // For modal spells, derive targeting from the chosen mode's unwrapped effect;
        // for non-modal spells, use the card's declared targeting (which accounts for auras, ETB effects, etc.)
        boolean unwrappedNeedsSpellTarget = wasModal
                ? filteredSpellEffects.stream().anyMatch(CardEffect::canTargetSpell)
                : EffectResolution.needsSpellTarget(card);
        // Per MTG rule 601.2c, only the spell itself determines whether a target is required
        // at cast time. ETB triggered abilities choose targets when they go on the stack after
        // the permanent enters, so isNeedsSpellCastTarget() (which excludes ETB effects) is correct.
        boolean unwrappedNeedsTarget = wasModal
                ? filteredSpellEffects.stream().anyMatch(e -> e.canTargetPermanent() || e.canTargetPlayer() || e.canTargetGraveyard())
                : EffectResolution.needsSpellCastTarget(card);

        // Validate alternate casting cost if used (e.g. Demon of Death's Gate)
        if (usingAlternateCost) {
            AlternateHandCast altCast = card.getCastingOption(AlternateHandCast.class)
                    .orElseThrow(() -> new IllegalStateException("Card does not have an alternate casting cost"));

            var sacCost = altCast.getCost(SacrificePermanentsCost.class);
            if (sacCost.isPresent()) {
                if (alternateCostSacrificePermanentIds.size() != sacCost.get().count()) {
                    throw new IllegalStateException("Must sacrifice exactly " + sacCost.get().count() + " permanents");
                }
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                for (UUID sacId : alternateCostSacrificePermanentIds) {
                    Permanent toSacrifice = battlefield.stream()
                            .filter(p -> p.getId().equals(sacId))
                            .findFirst()
                            .orElse(null);
                    if (toSacrifice == null) {
                        throw new IllegalStateException("Sacrifice target not found on your battlefield");
                    }
                    if (!gameQueryService.matchesPermanentPredicate(gameData, toSacrifice, sacCost.get().filter())) {
                        throw new IllegalStateException("Sacrifice target does not match the required filter");
                    }
                }
            }

            var lifeCost = altCast.getCost(LifeCastingCost.class);
            if (lifeCost.isPresent()) {
                int currentLife = gameData.getLife(playerId);
                if (currentLife < lifeCost.get().amount()) {
                    throw new IllegalStateException("Not enough life to pay alternate cost");
                }
            }

            var tapCost = altCast.getCost(TapUntappedPermanentsCost.class);
            if (tapCost.isPresent()) {
                int requiredCount = tapCost.get().count();
                // When sacrifice cost is also present, the IDs are used for sacrifice; tap IDs come separately
                // When only tap cost is present, all IDs are for tapping
                int sacCount = sacCost.map(SacrificePermanentsCost::count).orElse(0);
                int tapIdCount = alternateCostSacrificePermanentIds.size() - sacCount;
                if (tapIdCount != requiredCount) {
                    throw new IllegalStateException("Must tap exactly " + requiredCount + " permanents");
                }
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                List<UUID> tapIds = alternateCostSacrificePermanentIds.subList(sacCount, alternateCostSacrificePermanentIds.size());
                for (UUID tapId : tapIds) {
                    Permanent toTap = battlefield.stream()
                            .filter(p -> p.getId().equals(tapId))
                            .findFirst()
                            .orElse(null);
                    if (toTap == null) {
                        throw new IllegalStateException("Tap target not found on your battlefield");
                    }
                    if (toTap.isTapped()) {
                        throw new IllegalStateException("Permanent is already tapped");
                    }
                    if (!gameQueryService.matchesPermanentPredicate(gameData, toTap, tapCost.get().filter())) {
                        throw new IllegalStateException("Tap target does not match the required filter");
                    }
                }
            }

            var manaCost = altCast.getCost(ManaCastingCost.class);
            if (manaCost.isPresent()) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                ManaCost cost = new ManaCost(manaCost.get().manaCost());
                if (!cost.canPay(pool, 0)) {
                    throw new IllegalStateException("Not enough mana to pay alternate casting cost");
                }
            }
        }

        if (!usingAlternateCost && !gameBroadcastService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) {
            // Check if a non-zero alternative cost from the battlefield is affordable (e.g. Jodah)
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = gameBroadcastService.getCastCostModifier(gameData, playerId, card);
            boolean usingBattlefieldAlternativeCost = false;
            if (card.getManaCost() != null) {
                ManaCost normalCost = new ManaCost(card.getManaCost());
                if (!normalCost.canPay(pool, additionalCost)) {
                    usingBattlefieldAlternativeCost = gameBroadcastService.canAffordAlternativeCostFromBattlefield(
                            gameData, playerId, card, pool, additionalCost);
                }
            }

            if (!usingBattlefieldAlternativeCost) {
                // For X-cost spells, validate that player can pay colored + generic + xValue + any cost increases
                if (card.getManaCost() != null) {
                    ManaCost cost = new ManaCost(card.getManaCost());
                    if (cost.hasX()) {
                        if (effectiveXValue < 0) {
                            throw new IllegalStateException("X value cannot be negative");
                        }
                        int perTargetCost = card.getAdditionalCostPerExtraTarget() * Math.max(0, targetIds.size() - 1);
                        int totalAdditionalCost = additionalCost + perTargetCost;
                        ManaRestrictionFlags flags = computeManaRestrictionFlags(gameData, playerId, card, kicked);
                        if (card.getXColorRestriction() != null) {
                            if (!cost.canPay(pool, effectiveXValue, card.getXColorRestriction(), totalAdditionalCost)) {
                                throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                            }
                        } else if (flags.hasRestricted()) {
                            if (!cost.canPay(pool, effectiveXValue + totalAdditionalCost, flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(), flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless())) {
                                throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                            }
                        } else if (!cost.canPay(pool, effectiveXValue + totalAdditionalCost)) {
                            throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                        }
                    }
                }

                // Validate creature-only mana restriction (e.g. Myr Superion)
                if (card.isRequiresCreatureMana()) {
                    ManaCost creatureCost = new ManaCost(card.getManaCost());
                    int additionalCostForCreature = gameBroadcastService.getCastCostModifier(gameData, playerId, card);
                    if (!creatureCost.canPayCreatureOnly(pool, additionalCostForCreature)) {
                        throw new IllegalStateException("Can only spend mana produced by creatures to cast this spell");
                    }
                }
            }
        }

        // Validate spell target (targeting a spell on the stack)
        if (unwrappedNeedsSpellTarget) {
            targetLegalityService.validateSpellTargetOnStack(gameData, targetId, card.getTargetFilter(), playerId);
        }

        ReturnCardFromGraveyardEffect graveyardReturnEffect = (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.SPELL).stream()
                .filter(e -> e instanceof ReturnCardFromGraveyardEffect)
                .findFirst().orElse(null);
        boolean needsSingleGraveyardTargeting = graveyardReturnEffect != null;

        // Detect any effect that targets a graveyard card (e.g. PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect)
        boolean needsGraveyardEffectTargeting = !needsSingleGraveyardTargeting
                && card.getEffects(EffectSlot.SPELL).stream().anyMatch(e -> e.canTargetGraveyard());
        boolean canTargetAnyGraveyard = card.getEffects(EffectSlot.SPELL).stream().anyMatch(e -> e.canTargetAnyGraveyard());

        // Detect exile targeting effects (e.g. ReturnTargetCardFromExileToHandEffect)
        ReturnTargetCardFromExileToHandEffect exileReturnEffect = (ReturnTargetCardFromExileToHandEffect) card.getEffects(EffectSlot.SPELL).stream()
                .filter(e -> e instanceof ReturnTargetCardFromExileToHandEffect)
                .findFirst().orElse(null);
        boolean needsExileTargeting = exileReturnEffect != null;

        // Validate target if specified (can be a permanent or a player)
        if (targetId != null && !unwrappedNeedsSpellTarget) {
            if (needsExileTargeting) {
                if (exileReturnEffect.ownedOnly()) {
                    boolean inControllersExile = gameData.getPlayerExiledCards(playerId)
                            .stream()
                            .anyMatch(c -> c.getId().equals(targetId));
                    if (!inControllersExile) {
                        throw new IllegalStateException("Target must be an exiled card you own");
                    }
                }
                targetLegalityService.validateEffectTargetInZone(gameData, card, targetId, Zone.EXILE);
            } else if (needsSingleGraveyardTargeting) {
                String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                if (graveyardReturnEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
                    boolean inControllersGraveyard = gameData.playerGraveyards
                            .getOrDefault(playerId, List.of())
                            .stream()
                            .anyMatch(c -> c.getId().equals(targetId));
                    if (!inControllersGraveyard) {
                        throw new IllegalStateException("Target must be a " + filterLabel + " in your graveyard");
                    }
                }
                if (card.getMaxTargets() > 0) {
                    // Mixed graveyard + permanent targeting: validate only graveyard effects
                    targetLegalityService.validateGraveyardEffectTargetOnly(gameData, card, targetId);
                } else {
                    targetLegalityService.validateEffectTargetInZone(gameData, card, targetId, Zone.GRAVEYARD, effectiveXValue);
                }
            } else if (needsGraveyardEffectTargeting) {
                if (!canTargetAnyGraveyard) {
                    boolean inControllersGraveyard = gameData.playerGraveyards
                            .getOrDefault(playerId, List.of())
                            .stream()
                            .anyMatch(c -> c.getId().equals(targetId));
                    if (inControllersGraveyard) {
                        throw new IllegalStateException("Target must be in an opponent's graveyard");
                    }
                }
                if (card.getMaxTargets() > 0) {
                    // Mixed graveyard + permanent targeting: validate only graveyard effects
                    targetLegalityService.validateGraveyardEffectTargetOnly(gameData, card, targetId);
                } else {
                    targetLegalityService.validateEffectTargetInZone(gameData, card, targetId, Zone.GRAVEYARD);
                }
            } else {
                targetLegalityService.validateSpellTargeting(gameData, card, targetId, null, playerId, unwrappedNeedsTarget, effectiveXValue);
            }
        } else if (unwrappedNeedsTarget && needsExileTargeting) {
            String exileFilterLabel = CardPredicateUtils.describeFilter(exileReturnEffect.filter());
            throw new IllegalStateException(exileReturnEffect.ownedOnly()
                    ? "Must target an exiled " + exileFilterLabel + " you own"
                    : "Must target an exiled " + exileFilterLabel);
        } else if (unwrappedNeedsTarget && needsSingleGraveyardTargeting) {
            // For spells with multi-target permanent targeting, graveyard target is optional
            if (card.getMaxTargets() == 0) {
                String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                throw new IllegalStateException("Must target a " + filterLabel + " in your graveyard");
            }
        } else if (unwrappedNeedsTarget && needsGraveyardEffectTargeting) {
            // For spells with multi-target permanent targeting (e.g. Yawgmoth's Vile Offering),
            // graveyard target is optional when the spell's permanent target min allows it
            if (card.getMaxTargets() == 0) {
                throw new IllegalStateException("Must target a card in a graveyard");
            }
        }

        // Validate multi-target permanent targeting
        if (card.getMaxTargets() > 0 && !targetIds.isEmpty()) {
            targetLegalityService.validateMultiSpellTargets(gameData, card, targetIds, playerId);
        }

        // Validate permanent targets for spells that also target a spell on the stack (e.g. Lost in the Mist)
        if (unwrappedNeedsSpellTarget && unwrappedNeedsTarget && !targetIds.isEmpty()) {
            for (UUID permTargetId : targetIds) {
                targetLegalityService.validateSpellTargeting(gameData, card, permTargetId, null, playerId, true);
            }
        }

        // Validate and apply convoke
        List<ManaColor> convokeContributions = List.of();
        if (!convokeCreatureIds.isEmpty() && card.getKeywords().contains(Keyword.CONVOKE)) {
            List<ManaColor> contributions = new ArrayList<>();
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            List<Permanent> validatedCreatures = new ArrayList<>();
            for (UUID creatureId : convokeCreatureIds) {
                Permanent creature = battlefield.stream()
                        .filter(p -> p.getId().equals(creatureId))
                        .findFirst()
                        .orElse(null);
                if (creature == null) {
                    throw new IllegalStateException("Convoke creature not found on your battlefield");
                }
                if (!gameQueryService.isCreature(gameData, creature)) {
                    throw new IllegalStateException(creature.getCard().getName() + " is not a creature");
                }
                if (creature.isTapped()) {
                    throw new IllegalStateException(creature.getCard().getName() + " is already tapped");
                }
                CardColor creatureColor = creature.getEffectiveColor();
                contributions.add(creatureColor != null ? ManaColor.fromCode(creatureColor.getCode()) : null);
                validatedCreatures.add(creature);
            }
            // Tap all convoke creatures (after validation to ensure atomic failure)
            for (Permanent creature : validatedCreatures) {
                creature.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, creature);
            }
            convokeContributions = contributions;
        }

        // Validate graveyard targets for spells that target creature cards in graveyard
        boolean needsGraveyardCreatureTargeting = card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof ExileCreaturesFromGraveyardAndCreateTokensEffect);
        if (needsGraveyardCreatureTargeting && effectiveXValue > 0) {
            long creatureCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .count();
            if (effectiveXValue > creatureCount) {
                throw new IllegalStateException("Not enough creature cards in graveyard (need " + effectiveXValue + ", have " + creatureCount + ")");
            }
        }

        hand.remove(cardIndex);

        if (card.hasType(CardType.LAND)) {
            // Lands bypass the stack — go directly onto battlefield
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, new Permanent(card));
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);

            String logEntry = player.getUsername() + " plays " + card.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} plays {}", gameData.id, player.getUsername(), card.getName());

            // Process ETB effects for lands (e.g. Glimmerpost)
            battlefieldEntryService.processCreatureETBEffects(gameData, playerId, card, null, false);

            turnProgressionService.resolveAutoPass(gameData);
        } else if (card.hasType(CardType.CREATURE) || card.hasType(CardType.ENCHANTMENT)
                || card.hasType(CardType.ARTIFACT) || card.hasType(CardType.PLANESWALKER)) {
            // Permanent spells: pay mana (or alternate cost), put on stack, finish
            int manaCostX = (card.hasType(CardType.ARTIFACT)) ? effectiveXValue : 0;
            int stackX = (card.hasType(CardType.CREATURE) || card.hasType(CardType.ARTIFACT))
                    ? effectiveXValue : 0;
            UUID stackTarget = (card.hasType(CardType.PLANESWALKER)) ? null : targetId;

            int sacrificeCostReduction = 0;
            if (hasSacrificeForCostReduction) {
                sacrificeCostReduction = paySacrificeCreaturesForCostReduction(gameData, player, card, alternateCostSacrificePermanentIds);
            }

            if (usingAlternateCost) {
                payAlternateCastingCost(gameData, player, card, alternateCostSacrificePermanentIds);
            } else {
                paySpellManaCost(gameData, playerId, card, manaCostX, convokeContributions, phyrexianLifeCount, kicked, sacrificeCostReduction);
            }
            KickerEffect kickerEffect = findKickerEffect(card);
            if (kicked && kickerEffect != null) {
                payKickerCost(gameData, player, card, kickerEffect, sacrificePermanentId);
            }
            payExileGraveyardCost(gameData, player, card, exileGraveyardCost, exileGraveyardCardIndex, 0);
            payExileNCardsFromGraveyardCost(gameData, player, card, exileNCardsGraveyardCost, exileGraveyardCardIndices);
            StackEntry entry;
            if (!targetIds.isEmpty()) {
                // Multi-target creature (e.g. Burning Sun's Avatar ETB with multiple targets)
                entry = new StackEntry(
                        cardTypeToStackEntryType(card.getType()), card, playerId, card.getName(),
                        List.of(), stackX, targetIds
                );
            } else {
                entry = new StackEntry(
                        cardTypeToStackEntryType(card.getType()), card, playerId, card.getName(),
                        List.of(), stackX, stackTarget, null
                );
            }
            if (kicked && kickerEffect != null) {
                entry.setKicked(true);
            }
            gameData.stack.add(entry);
            finishSpellCast(gameData, playerId, player, hand, card);
        } else if (card.hasType(CardType.SORCERY) || card.hasType(CardType.INSTANT)) {
            // Sorcery/Instant spells: pay mana + sacrifice costs, handle targeting, put on stack
            StackEntryType entryType = cardTypeToStackEntryType(card.getType());
            int resolvedXValue = effectiveXValue;
            int perTargetCost = card.getAdditionalCostPerExtraTarget() * Math.max(0, targetIds.size() - 1);
            if (usingAlternateCost) {
                payAlternateCastingCost(gameData, player, card, alternateCostSacrificePermanentIds);
            } else {
                paySpellManaCost(gameData, playerId, card, resolvedXValue + perTargetCost, convokeContributions, phyrexianLifeCount, kicked);
            }
            resolvedXValue = payAllSacrificeCosts(gameData, player, card, sacrificePermanentId, sacFlags, resolvedXValue);
            resolvedXValue = payExileGraveyardCost(gameData, player, card, exileGraveyardCost, exileGraveyardCardIndex, resolvedXValue);
            resolvedXValue = payExileXCardsFromGraveyardCost(gameData, player, card, exileXCardsGraveyardCost, exileGraveyardCardIndices, resolvedXValue);
            payExileNCardsFromGraveyardCost(gameData, player, card, exileNCardsGraveyardCost, exileGraveyardCardIndices);
            KickerEffect kickerEffect = findKickerEffect(card);
            if (kicked && kickerEffect != null) {
                payKickerCost(gameData, player, card, kickerEffect, sacrificePermanentId);
            }

            // Check for "up to N target cards from all graveyards" pile separation spells (e.g. Boneyard Parley)
            ExileTargetGraveyardCardsAndSeparateIntoPilesEffect pileSeparationEffect =
                    (ExileTargetGraveyardCardsAndSeparateIntoPilesEffect) filteredSpellEffects.stream()
                            .filter(ExileTargetGraveyardCardsAndSeparateIntoPilesEffect.class::isInstance)
                            .findFirst().orElse(null);

            // Check for "up to N target cards from graveyard" spells (e.g. Morbid Plunder)
            ReturnTargetCardsFromGraveyardToHandEffect graveyardToHandEffect =
                    (ReturnTargetCardsFromGraveyardToHandEffect) filteredSpellEffects.stream()
                            .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                            .findFirst().orElse(null);

            // Check for "any number of target cards from graveyard" spells (e.g. Frantic Salvage)
            PutTargetCardsFromGraveyardOnTopOfLibraryEffect graveyardToTopEffect =
                    (PutTargetCardsFromGraveyardOnTopOfLibraryEffect) card.getEffects(EffectSlot.SPELL).stream()
                            .filter(PutTargetCardsFromGraveyardOnTopOfLibraryEffect.class::isInstance)
                            .findFirst().orElse(null);

            // Check for "target player shuffles up to N cards from their graveyard" spells (e.g. Memory's Journey)
            ShuffleTargetCardsFromGraveyardIntoLibraryEffect shuffleGraveyardCardsEffect =
                    (ShuffleTargetCardsFromGraveyardIntoLibraryEffect) filteredSpellEffects.stream()
                            .filter(ShuffleTargetCardsFromGraveyardIntoLibraryEffect.class::isInstance)
                            .findFirst().orElse(null);

            if (pileSeparationEffect != null) {
                // Target up to N creature cards from ALL graveyards
                long matchingCount = 0;
                for (UUID pid : gameData.orderedPlayerIds) {
                    matchingCount += gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                            .filter(c -> gameQueryService.matchesCardPredicate(c, pileSeparationEffect.filter(), card.getId()))
                            .count();
                }
                if (matchingCount > 0) {
                    battlefieldEntryService.handleUpToNAllGraveyardsSpellTargeting(gameData, playerId, card,
                            entryType, pileSeparationEffect.filter(),
                            pileSeparationEffect.maxTargets(), filteredSpellEffects);
                    return; // finishSpellCast handled in handleMultipleGraveyardCardsChosen
                }
                // No matching cards in any graveyard — put spell on stack with 0 targets
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (shuffleGraveyardCardsEffect != null) {
                // Target player is specified via targetId
                UUID targetGraveyardOwner = targetId;
                if (targetGraveyardOwner == null) {
                    throw new IllegalStateException("Must target a player");
                }
                long matchingCount = gameData.playerGraveyards.getOrDefault(targetGraveyardOwner, List.of()).stream()
                        .filter(c -> gameQueryService.matchesCardPredicate(c, shuffleGraveyardCardsEffect.filter(), card.getId()))
                        .count();
                if (matchingCount > 0) {
                    battlefieldEntryService.handleUpToNTargetPlayerGraveyardSpellTargeting(gameData, playerId,
                            targetGraveyardOwner, card, entryType, shuffleGraveyardCardsEffect.filter(),
                            shuffleGraveyardCardsEffect.maxTargets(), filteredSpellEffects);
                    return; // finishSpellCast handled in handleMultipleGraveyardCardsChosen
                }
                // No matching cards in target player's graveyard — put spell on stack with 0 targets
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, targetId,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (graveyardToHandEffect != null) {
                long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                        .filter(c -> gameQueryService.matchesCardPredicate(c, graveyardToHandEffect.filter(), card.getId()))
                        .count();
                if (matchingCount > 0) {
                    battlefieldEntryService.handleUpToNGraveyardSpellTargeting(gameData, playerId, card,
                            entryType, graveyardToHandEffect.filter(),
                            graveyardToHandEffect.maxTargets(), filteredSpellEffects);
                    return; // finishSpellCast handled in handleMultipleGraveyardCardsChosen
                }
                // No matching cards — put spell on stack with 0 targets (fizzles on resolution)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (graveyardToTopEffect != null) {
                long matchingCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                        .filter(c -> gameQueryService.matchesCardPredicate(c, graveyardToTopEffect.filter(), card.getId()))
                        .count();
                if (matchingCount > 0) {
                    battlefieldEntryService.handleAnyNumberGraveyardSpellTargeting(gameData, playerId, card,
                            entryType, graveyardToTopEffect.filter());
                    return; // finishSpellCast handled in handleMultipleGraveyardCardsChosen
                }
                // No matching cards — put spell on stack with 0 targets (still draws, etc.)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, Map.of(), null, List.of(), List.of()
                ));
            } else if (needsGraveyardCreatureTargeting && resolvedXValue > 0) {
                // Prompt player to choose graveyard targets before putting spell on stack
                battlefieldEntryService.handleGraveyardSpellTargeting(gameData, playerId, card,
                        entryType, resolvedXValue);
                return; // finishSpellCast handled in graveyard targeting callback
            } else if (needsGraveyardCreatureTargeting) {
                // X=0: no targets needed, put spell on stack directly (resolves doing nothing)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, null, null, List.of(), List.of()
                ));
            } else if (kicked && damageAssignments != null && !damageAssignments.isEmpty()
                    && findKickedDividedDamageEffect(filteredSpellEffects) != null) {
                // Kicked spell with divided damage among any targets (e.g. Fight with Fire)
                DealDividedDamageAmongAnyTargetsEffect dividedEffect = findKickedDividedDamageEffect(filteredSpellEffects);
                int totalDamage = damageAssignments.values().stream().mapToInt(Integer::intValue).sum();
                if (totalDamage != dividedEffect.totalDamage()) {
                    throw new IllegalStateException("Damage assignments must sum to " + dividedEffect.totalDamage());
                }
                for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                    UUID target = assignment.getKey();
                    boolean isPlayer = gameData.playerIds.contains(target);
                    if (!isPlayer) {
                        Permanent perm = gameQueryService.findPermanentById(gameData, target);
                        if (perm == null) {
                            throw new IllegalStateException("Invalid target");
                        }
                    }
                    if (assignment.getValue() <= 0) {
                        throw new IllegalStateException("Each damage assignment must be positive");
                    }
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, null, damageAssignments
                ));
            } else if (EffectResolution.needsDamageDistribution(card)) {
                // Validate damage assignments for damage distribution spells
                if (damageAssignments == null || damageAssignments.isEmpty()) {
                    throw new IllegalStateException("Damage assignments required");
                }

                DealDividedDamageAmongTargetCreaturesEffect dividedCreatureEffect = filteredSpellEffects.stream()
                        .filter(e -> e instanceof DealDividedDamageAmongTargetCreaturesEffect)
                        .map(DealDividedDamageAmongTargetCreaturesEffect.class::cast)
                        .findFirst().orElse(null);

                int totalDamage = damageAssignments.values().stream().mapToInt(Integer::intValue).sum();

                if (dividedCreatureEffect != null) {
                    // Fixed-damage divided damage spell (e.g. Ignite Disorder)
                    if (totalDamage != dividedCreatureEffect.totalDamage()) {
                        throw new IllegalStateException("Damage assignments must sum to " + dividedCreatureEffect.totalDamage());
                    }
                    if (damageAssignments.size() > card.getMaxTargets()) {
                        throw new IllegalStateException("Too many targets");
                    }
                    for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                        Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
                        if (target == null || !gameQueryService.isCreature(gameData, target)) {
                            throw new IllegalStateException("All targets must be creatures");
                        }
                        if (card.getTargetFilter() != null) {
                            gameQueryService.validateTargetFilter(gameData, card.getTargetFilter(), target);
                        }
                        if (assignment.getValue() <= 0) {
                            throw new IllegalStateException("Each damage assignment must be positive");
                        }
                    }
                } else {
                    // X-damage attacking creature divided damage spell (e.g. Hail of Arrows)
                    if (totalDamage != resolvedXValue) {
                        throw new IllegalStateException("Damage assignments must sum to X (" + resolvedXValue + ")");
                    }
                    for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                        Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
                        if (target == null || !gameQueryService.isCreature(gameData, target) || !target.isAttacking()) {
                            throw new IllegalStateException("All targets must be attacking creatures");
                        }
                        if (assignment.getValue() <= 0) {
                            throw new IllegalStateException("Each damage assignment must be positive");
                        }
                    }
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, null, damageAssignments
                ));
            } else if (unwrappedNeedsSpellTarget) {
                if (unwrappedNeedsTarget && !targetIds.isEmpty()) {
                    // Spell targets both a spell on the stack and permanent(s) (e.g. Lost in the Mist)
                    gameData.stack.add(new StackEntry(
                            entryType, card, playerId, card.getName(),
                            filteredSpellEffects, resolvedXValue, targetId,
                            null, Map.of(), Zone.STACK, List.of(), targetIds
                    ));
                } else {
                    gameData.stack.add(new StackEntry(
                            entryType, card, playerId, card.getName(),
                            filteredSpellEffects, resolvedXValue, targetId,
                            null, Map.of(), Zone.STACK, List.of(), List.of()
                    ));
                }
            } else if (kicked && targetId != null && !targetIds.isEmpty()) {
                // Kicked spell with primary target + additional kicked target(s)
                // (e.g. Goblin Barrage: primary = creature, kicked = player)
                for (UUID kickerTargetId : targetIds) {
                    if (!gameData.playerIds.contains(kickerTargetId)) {
                        Permanent kickerTarget = gameQueryService.findPermanentById(gameData, kickerTargetId);
                        if (kickerTarget == null) {
                            throw new IllegalStateException("Invalid kicker target");
                        }
                    }
                }
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId,
                        null, Map.of(), null, List.of(), targetIds
                ));
            } else if (!targetIds.isEmpty() && (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting) && targetId != null) {
                // Combined graveyard + permanent targeting (e.g. Yawgmoth's Vile Offering)
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId,
                        null, Map.of(), Zone.GRAVEYARD, List.of(), targetIds
                ));
            } else if (!targetIds.isEmpty() && !sacFlags.usesSacrificeAllCreaturesCost()) {
                // Multi-target spell (e.g. "one or two target creatures each get +2/+1")
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetIds
                ));
            } else if (needsExileTargeting) {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId, null,
                        Map.of(), Zone.EXILE, List.of(), List.of()
                ));
            } else if (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting) {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId, null,
                        Map.of(), Zone.GRAVEYARD, List.of(), List.of()
                ));
            } else {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetId, null
                ));
            }
            if (kicked && kickerEffect != null && !gameData.stack.isEmpty()) {
                gameData.stack.getLast().setKicked(true);
            }
            finishSpellCast(gameData, playerId, player, hand, card);
        }
    }

    // --- Sacrifice cost payment ---

    private int payAllSacrificeCosts(GameData gameData, Player player, Card card,
                                     UUID sacrificePermanentId, SacrificeCostFlags sacFlags, int resolvedXValue) {
        if (sacFlags.usesSacrificeCreatureCost()) {
            SacrificedCreatureStats stats = paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    "a creature", p -> gameQueryService.isCreature(gameData, p));
            SacrificeCreatureCost sacCreatureCost = (SacrificeCreatureCost) card.getEffects(EffectSlot.SPELL).stream()
                    .filter(SacrificeCreatureCost.class::isInstance)
                    .findFirst().orElseThrow();
            if (sacCreatureCost.trackSacrificedPower()) {
                resolvedXValue = stats.power();
            }
            if (sacCreatureCost.trackSacrificedToughness()) {
                resolvedXValue = stats.toughness();
            }
        }
        if (sacFlags.usesSacrificeArtifactCost()) {
            paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    "an artifact", p -> gameQueryService.isArtifact(p));
        }
        if (sacFlags.sacrificePermanentCost() != null) {
            paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    sacFlags.sacrificePermanentCost().description(),
                    p -> gameQueryService.matchesPermanentPredicate(gameData, p, sacFlags.sacrificePermanentCost().filter()));
        }
        if (sacFlags.usesSacrificeAllCreaturesCost()) {
            resolvedXValue = paySacrificeAllCreaturesYouControlCost(gameData, player, card);
        }
        return resolvedXValue;
    }

    private record SacrificedCreatureStats(int power, int toughness) {}

    private SacrificedCreatureStats paySingleSacrificeCost(GameData gameData, Player player, Card sourceCard,
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
        int power = gameQueryService.getEffectivePower(gameData, toSacrifice);
        int toughness = gameQueryService.getEffectiveToughness(gameData, toSacrifice);
        if (permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    player.getUsername() + " sacrifices " + toSacrifice.getCard().getName()
                            + " for " + sourceCard.getName() + ".");
        }
        return new SacrificedCreatureStats(power, toughness);
    }

    private int paySacrificeAllCreaturesYouControlCost(GameData gameData, Player player, Card sourceCard) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> creaturesToSacrifice = battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .toList();

        // Snapshot total power first, because all chosen creatures are sacrificed together.
        int totalPower = 0;
        for (Permanent creature : creaturesToSacrifice) {
            totalPower += gameQueryService.getEffectivePower(gameData, creature);
        }
        for (Permanent creature : creaturesToSacrifice) {
            if (permanentRemovalService.removePermanentToGraveyard(gameData, creature)) {
                String logEntry = player.getUsername() + " sacrifices " + creature.getCard().getName()
                        + " for " + sourceCard.getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }
        return Math.max(0, totalPower);
    }

    private int paySacrificeCreaturesForCostReduction(GameData gameData, Player player, Card card, List<UUID> sacrificeIds) {
        SacrificeCreaturesForCostReductionEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(SacrificeCreaturesForCostReductionEffect.class::isInstance)
                .map(SacrificeCreaturesForCostReductionEffect.class::cast)
                .findFirst().orElseThrow();

        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        int sacrificedCount = 0;

        for (UUID sacId : sacrificeIds) {
            Permanent toSacrifice = battlefield.stream()
                    .filter(p -> p.getId().equals(sacId))
                    .findFirst().orElse(null);
            if (toSacrifice == null) {
                throw new IllegalStateException("Sacrifice target not found on battlefield");
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, sacId);
            if (!playerId.equals(controllerId)) {
                throw new IllegalStateException("Can only sacrifice permanents you control");
            }
            if (!gameQueryService.isCreature(gameData, toSacrifice)) {
                throw new IllegalStateException("Can only sacrifice creatures for cost reduction");
            }
            if (permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        player.getUsername() + " sacrifices " + toSacrifice.getCard().getName()
                                + " to reduce the cost of " + card.getName() + ".");
                sacrificedCount++;
            }
        }

        return sacrificedCount * effect.reductionPerCreature();
    }

    private int payExileGraveyardCost(GameData gameData, Player player, Card card,
                                       ExileCardFromGraveyardCost cost, Integer exileGraveyardCardIndex, int resolvedXValue) {
        if (cost == null) return resolvedXValue;
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
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
        int exiledPower = exiledCard.getPower() != null ? exiledCard.getPower() : 0;
        graveyard.remove((int) exileGraveyardCardIndex);
        gameData.addToExile(playerId, exiledCard);
        gameBroadcastService.logAndBroadcast(gameData,
                player.getUsername() + " exiles " + exiledCard.getName() + " from graveyard for " + card.getName() + ".");
        if (cost.trackExiledPower()) {
            resolvedXValue = exiledPower;
        }
        return resolvedXValue;
    }

    private int payExileXCardsFromGraveyardCost(GameData gameData, Player player, Card card,
                                                 ExileXCardsFromGraveyardCost cost, List<Integer> exileGraveyardCardIndices, int resolvedXValue) {
        if (cost == null) return resolvedXValue;
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
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
        // Remove in descending index order so earlier indices remain valid
        List<Integer> sortedDescending = exileGraveyardCardIndices.stream().sorted(java.util.Comparator.reverseOrder()).toList();
        List<Card> exiledCards = new ArrayList<>();
        for (int idx : sortedDescending) {
            Card exiledCard = graveyard.remove(idx);
            exiledCards.add(exiledCard);
        }
        for (Card exiledCard : exiledCards) {
            gameData.addToExile(playerId, exiledCard);
            gameBroadcastService.logAndBroadcast(gameData,
                    player.getUsername() + " exiles " + exiledCard.getName() + " from graveyard for " + card.getName() + ".");
        }
        return exiledCards.size();
    }

    private void payExileNCardsFromGraveyardCost(GameData gameData, Player player, Card card,
                                                  ExileNCardsFromGraveyardCost cost, List<Integer> exileGraveyardCardIndices) {
        if (cost == null) return;
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (exileGraveyardCardIndices == null || exileGraveyardCardIndices.size() != cost.count()) {
            throw new IllegalStateException("Must exile exactly " + cost.count() + " "
                    + (cost.requiredType() != null ? cost.requiredType().name().toLowerCase() + " " : "")
                    + "cards from your graveyard to cast " + card.getName());
        }
        if (graveyard == null || graveyard.size() < cost.count()) {
            throw new IllegalStateException("Not enough cards in graveyard to exile");
        }
        if (exileGraveyardCardIndices.stream().distinct().count() != exileGraveyardCardIndices.size()) {
            throw new IllegalStateException("Duplicate graveyard card indices");
        }
        for (int idx : exileGraveyardCardIndices) {
            if (idx < 0 || idx >= graveyard.size()) {
                throw new IllegalStateException("Invalid graveyard card index: " + idx);
            }
            if (cost.requiredType() != null && !graveyard.get(idx).hasType(cost.requiredType())) {
                String typeName = cost.requiredType().name().toLowerCase();
                throw new IllegalStateException("Must exile a " + typeName + " card from your graveyard");
            }
        }
        // Remove in descending index order so earlier indices remain valid
        List<Integer> sortedDescending = exileGraveyardCardIndices.stream().sorted(java.util.Comparator.reverseOrder()).toList();
        List<Card> exiledCards = new ArrayList<>();
        for (int idx : sortedDescending) {
            Card exiledCard = graveyard.remove(idx);
            exiledCards.add(exiledCard);
        }
        for (Card exiledCard : exiledCards) {
            gameData.addToExile(playerId, exiledCard);
            gameBroadcastService.logAndBroadcast(gameData,
                    player.getUsername() + " exiles " + exiledCard.getName() + " from graveyard for " + card.getName() + ".");
        }
    }

    // --- Play with flashback from graveyard ---

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetId) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, List.of(), null, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetId, List<UUID> targetIds) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds, null, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds, exileGraveyardCardIndices, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (targetIds == null) targetIds = List.of();
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        // Ashes of the Abhorrent etc.: players can't cast spells from graveyards
        if (!gameQueryService.canPlayersCastSpellsFromGraveyards(gameData)) {
            throw new IllegalStateException("Spells can't be cast from graveyards");
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null || graveyardCardIndex < 0 || graveyardCardIndex >= graveyard.size()) {
            throw new IllegalArgumentException("Invalid graveyard card index");
        }

        Card card = graveyard.get(graveyardCardIndex);
        var flashbackOpt = card.getCastingOption(FlashbackCast.class);
        var graveyardCastOpt = card.getCastingOption(GraveyardCast.class);
        boolean grantedFlashback = flashbackOpt.isEmpty()
                && gameData.cardsGrantedFlashbackUntilEndOfTurn.contains(card.getId());
        boolean emblemFlashback = flashbackOpt.isEmpty() && !grantedFlashback
                && gameBroadcastService.hasEmblemGrantedFlashback(gameData, playerId, card);
        boolean isGraveyardCast = graveyardCastOpt.isPresent() && flashbackOpt.isEmpty()
                && !grantedFlashback && !emblemFlashback;

        // Check if this card is castable via a Muldrotha-style static graveyard permanent cast effect
        boolean isGrantedGraveyardCast = false;
        Optional<UUID> graveyardCastSourceId = Optional.empty();
        if (flashbackOpt.isEmpty() && !grantedFlashback && !emblemFlashback && !isGraveyardCast) {
            graveyardCastSourceId = gameBroadcastService.findGraveyardCastSourcePermanentId(gameData, playerId);
            if (graveyardCastSourceId.isPresent()) {
                Set<CardType> typesCastFromGraveyard = gameData.permanentTypesCastFromGraveyardThisTurn
                        .getOrDefault(graveyardCastSourceId.get(), Set.of());
                isGrantedGraveyardCast = GameBroadcastService.hasUnusedPermanentTypeSlot(card, typesCastFromGraveyard);
            }
        }

        if (flashbackOpt.isEmpty() && !grantedFlashback && !emblemFlashback && !isGraveyardCast && !isGrantedGraveyardCast) {
            throw new IllegalStateException("Card cannot be cast from graveyard");
        }

        // Validate timing
        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        boolean isInstantSpeed = card.hasType(CardType.INSTANT);
        if (!isInstantSpeed && !(isActivePlayer && isMainPhase && stackEmpty)) {
            throw new IllegalStateException("Cannot cast sorcery-speed spell from graveyard now");
        }

        // Validate and pay mana cost (graveyard cast, granted flashback, emblem flashback,
        // and granted graveyard cast all use the card's normal mana cost)
        String manaCostStr = (isGraveyardCast || grantedFlashback || emblemFlashback || isGrantedGraveyardCast)
                ? card.getManaCost()
                : flashbackOpt.get().getCost(ManaCastingCost.class)
                        .orElseThrow(() -> new IllegalStateException("Flashback has no mana cost"))
                        .manaCost();
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int additionalCost = gameBroadcastService.getCastCostModifier(gameData, playerId, card);
        if (!cost.canPay(pool, effectiveXValue + additionalCost)) {
            throw new IllegalStateException("Not enough mana to pay " + (isGraveyardCast || isGrantedGraveyardCast ? "casting" : "flashback") + " cost");
        }
        cost.pay(pool, effectiveXValue + additionalCost);

        // Remove card from graveyard
        graveyard.remove(graveyardCardIndex);

        // Pay exile-N-cards-from-graveyard cost if present (e.g. Skaab Ruinator)
        List<CardEffect> spellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        ExileNCardsFromGraveyardCost exileNCost = extractAndRemoveExileNCardsFromGraveyardCost(spellEffects);
        payExileNCardsFromGraveyardCost(gameData, player, card, exileNCost, exileGraveyardCardIndices);

        if (isGraveyardCast || isGrantedGraveyardCast) {
            // GraveyardCast / granted graveyard cast: permanent spell — enters battlefield on resolution, no exile
            if (isGrantedGraveyardCast && graveyardCastSourceId.isPresent()) {
                // Track which permanent type slot was used, keyed by the granting permanent's UUID
                Set<CardType> typesCastFromGraveyard = gameData.permanentTypesCastFromGraveyardThisTurn
                        .computeIfAbsent(graveyardCastSourceId.get(), k -> ConcurrentHashMap.newKeySet());
                if (chosenGraveyardType != null) {
                    // Player chose which type slot to use (for multi-type cards)
                    if (!card.hasType(chosenGraveyardType) || !chosenGraveyardType.isPermanentType()
                            || chosenGraveyardType == CardType.LAND || typesCastFromGraveyard.contains(chosenGraveyardType)) {
                        throw new IllegalStateException("Invalid chosen graveyard type: " + chosenGraveyardType);
                    }
                    typesCastFromGraveyard.add(chosenGraveyardType);
                } else {
                    // Auto-pick the first available type (for single-type cards or when no choice provided)
                    CardType primary = card.getType();
                    if (primary.isPermanentType() && primary != CardType.LAND && !typesCastFromGraveyard.contains(primary)) {
                        typesCastFromGraveyard.add(primary);
                    } else {
                        card.getAdditionalTypes().stream()
                                .filter(t -> t.isPermanentType() && t != CardType.LAND && !typesCastFromGraveyard.contains(t))
                                .findFirst()
                                .ifPresent(typesCastFromGraveyard::add);
                    }
                }
            }
            StackEntryType entryType = cardTypeToStackEntryType(card.getType());
            StackEntry stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    List.of(), 0, targetId, null
            );
            gameData.stack.add(stackEntry);
            finishSpellCast(gameData, playerId, player, graveyard, card, false);
            return;
        }

        StackEntryType entryType = card.hasType(CardType.INSTANT) ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;

        // Check for "target player shuffles up to N cards from their graveyard" flashback spells (e.g. Memory's Journey)
        ShuffleTargetCardsFromGraveyardIntoLibraryEffect shuffleGraveyardCardsEffect =
                (ShuffleTargetCardsFromGraveyardIntoLibraryEffect) spellEffects.stream()
                        .filter(ShuffleTargetCardsFromGraveyardIntoLibraryEffect.class::isInstance)
                        .findFirst().orElse(null);

        if (shuffleGraveyardCardsEffect != null) {
            UUID targetGraveyardOwner = targetId;
            if (targetGraveyardOwner == null) {
                throw new IllegalStateException("Must target a player");
            }
            long matchingCount = gameData.playerGraveyards.getOrDefault(targetGraveyardOwner, List.of()).stream()
                    .filter(c -> gameQueryService.matchesCardPredicate(c, shuffleGraveyardCardsEffect.filter(), card.getId()))
                    .count();
            if (matchingCount > 0) {
                battlefieldEntryService.handleUpToNTargetPlayerGraveyardSpellTargeting(gameData, playerId,
                        targetGraveyardOwner, card, entryType, shuffleGraveyardCardsEffect.filter(),
                        shuffleGraveyardCardsEffect.maxTargets(), spellEffects);
                gameData.graveyardTargetOperation.flashback = true;
                return; // finishSpellCast handled in handleMultipleGraveyardCardsChosen
            }
            // No matching cards — put on stack with 0 targets
            StackEntry stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    spellEffects, 0, targetId,
                    null, Map.of(), null, List.of(), List.of()
            );
            stackEntry.setCastWithFlashback(true);
            gameData.stack.add(stackEntry);
            finishSpellCast(gameData, playerId, player, graveyard, card, false);
            return;
        }

        StackEntry stackEntry;
        if (!targetIds.isEmpty()) {
            // Multi-target flashback spell
            if (card.getMaxTargets() > 0) {
                targetLegalityService.validateMultiSpellTargets(gameData, card, targetIds, playerId);
            }
            stackEntry = new StackEntry(
                    entryType, card, playerId, card.getName(),
                    spellEffects, effectiveXValue, targetIds
            );
        } else {
            // Single-target or no-target flashback spell
            boolean needsGraveyardEffectTargeting = spellEffects.stream().anyMatch(CardEffect::canTargetGraveyard);
            if (targetId != null && EffectResolution.needsTarget(card) && needsGraveyardEffectTargeting) {
                targetLegalityService.validateEffectTargetInZone(gameData, card, targetId, Zone.GRAVEYARD);
            } else if (targetId != null && EffectResolution.needsTarget(card)) {
                targetLegalityService.validateSpellTargeting(gameData, card, targetId, null, playerId, true);
            } else if (EffectResolution.needsTarget(card) && targetId == null) {
                throw new IllegalStateException("Spell requires a target");
            }
            if (needsGraveyardEffectTargeting) {
                stackEntry = new StackEntry(
                        entryType, card, playerId, card.getName(),
                        spellEffects, effectiveXValue, targetId, null,
                        Map.of(), Zone.GRAVEYARD, List.of(), List.of()
                );
            } else {
                stackEntry = new StackEntry(
                        entryType, card, playerId, card.getName(),
                        spellEffects, effectiveXValue, targetId, null
                );
            }
        }
        stackEntry.setCastWithFlashback(true);
        gameData.stack.add(stackEntry);

        finishSpellCast(gameData, playerId, player, graveyard, card, false);
    }

    // --- Play from exile ---

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue, UUID targetId) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();

        // Find the card in exile
        List<Card> exiledCards = gameData.getPlayerExiledCards(playerId);
        if (exiledCards.isEmpty()) {
            throw new IllegalStateException("No exiled cards");
        }

        UUID permittedPlayer = gameData.exilePlayPermissions.get(exileCardId);
        boolean hasPermission = (permittedPlayer != null && permittedPlayer.equals(playerId))
                || hasCastFromExiledWithSourcePermission(gameData, playerId, exileCardId);
        // Permission check is deferred until after we find the card — ExileCast cards bypass it

        Card card = exiledCards.stream()
                .filter(c -> c.getId().equals(exileCardId))
                .findFirst()
                .orElse(null);
        if (card == null) {
            throw new IllegalStateException("Card not found in exile");
        }

        boolean hasExileCast = card.getCastingOption(ExileCast.class).isPresent();
        if (!hasPermission && !hasExileCast) {
            throw new IllegalStateException("No permission to play this exiled card");
        }

        // Validate timing for ExileCast cards (creature/sorcery require sorcery-speed timing)
        if (hasExileCast && !card.hasType(CardType.LAND)) {
            boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
            boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                    || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
            boolean stackEmpty = gameData.stack.isEmpty();
            boolean isInstantSpeed = card.hasType(CardType.INSTANT)
                    || card.getKeywords().contains(Keyword.FLASH);
            if (!isInstantSpeed && !(isActivePlayer && isMainPhase && stackEmpty)) {
                throw new IllegalStateException("Cannot cast sorcery-speed spell from exile now");
            }
        }

        // Remove from exile and clean up permission
        gameData.removeFromExile(exileCardId);
        gameData.exilePlayPermissions.remove(exileCardId);

        if (card.hasType(CardType.LAND)) {
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, new Permanent(card));
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);

            String logEntry = player.getUsername() + " plays " + card.getName() + " from exile.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} plays {} from exile", gameData.id, player.getUsername(), card.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, playerId, card, null, false);
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        // Pay mana cost
        paySpellManaCost(gameData, playerId, card, effectiveXValue, List.of(), null);

        StackEntryType entryType = cardTypeToStackEntryType(card.getType());

        // Sorceries and instants need their spell effects for resolution;
        // permanent spells (creature, enchantment, artifact, planeswalker) use List.of()
        // because they resolve by entering the battlefield, not via effects.
        List<CardEffect> effectsToResolve;
        if (card.hasType(CardType.SORCERY) || card.hasType(CardType.INSTANT)) {
            effectsToResolve = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
            extractAndRemoveSacrificeCosts(effectsToResolve);
            effectiveXValue = unwrapChooseOneEffect(card, effectsToResolve, effectiveXValue);
        } else {
            effectsToResolve = List.of();
        }

        gameData.stack.add(new StackEntry(
                entryType, card, playerId, card.getName(),
                effectsToResolve, effectiveXValue, targetId, null
        ));

        // Use null hand list — card was already removed from exile
        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        String logEntry = player.getUsername() + " casts " + card.getName() + " from exile.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} casts {} from exile", gameData.id, player.getUsername(), card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private boolean hasCastFromExiledWithSourcePermission(GameData gameData, UUID playerId, UUID cardId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            boolean hasEffect = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof AllowCastFromCardsExiledWithSourceEffect);
            if (hasEffect) {
                List<Card> exiledWithPerm = gameData.getCardsExiledByPermanent(perm.getId());
                for (Card c : exiledWithPerm) {
                    if (c.getId().equals(cardId)) return true;
                }
            }
        }
        return false;
    }

    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();

        // Verify the player can cast from top of library
        Set<CardType> castableTypes = gameBroadcastService.getCastableTypesFromTopOfLibrary(gameData, playerId);
        if (castableTypes.isEmpty()) {
            throw new IllegalStateException("No effect allowing cast from library top");
        }

        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            throw new IllegalStateException("Library is empty");
        }

        Card card = deck.getFirst();

        // Validate card type matches
        boolean matchesType = castableTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(castableTypes::contains);
        if (!matchesType) {
            throw new IllegalStateException("Top card type is not castable from library");
        }

        // Remove from library
        deck.removeFirst();

        // Pay mana cost
        paySpellManaCost(gameData, playerId, card, effectiveXValue, List.of(), null);

        StackEntryType entryType = cardTypeToStackEntryType(card.getType());

        List<CardEffect> effectsToResolve;
        if (card.hasType(CardType.SORCERY) || card.hasType(CardType.INSTANT)) {
            effectsToResolve = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
            extractAndRemoveSacrificeCosts(effectsToResolve);
            effectiveXValue = unwrapChooseOneEffect(card, effectsToResolve, effectiveXValue);
        } else {
            effectsToResolve = List.of();
        }

        gameData.stack.add(new StackEntry(
                entryType, card, playerId, card.getName(),
                effectsToResolve, effectiveXValue, targetId, null
        ));

        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        String logEntry = player.getUsername() + " casts " + card.getName() + " from the top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} casts {} from library top", gameData.id, player.getUsername(), card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    // --- Mana payment ---

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, null, false);
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, phyrexianLifeCount, false);
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount, boolean kicked) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, phyrexianLifeCount, kicked, 0);
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount, boolean kicked, int extraCostReduction) {
        if (card.getManaCost() == null) return;
        // Alternative zero cost (e.g. Rooftop Storm): skip mana payment entirely
        if (gameBroadcastService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) return;
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int additionalCost = gameBroadcastService.getCastCostModifier(gameData, playerId, card) - extraCostReduction;
        ManaRestrictionFlags flags = computeManaRestrictionFlags(gameData, playerId, card, kicked);

        // Check if we should use a non-zero alternative cost from the battlefield (e.g. Jodah)
        // Use the alternative cost if the normal cost can't be paid but the alternative can
        if (!cost.canPay(pool, additionalCost)) {
            String altCostStr = gameBroadcastService.findAffordableAlternativeCostFromBattlefield(
                    gameData, playerId, card, pool, additionalCost);
            if (altCostStr != null) {
                ManaCost altCost = new ManaCost(altCostStr);
                altCost.pay(pool, additionalCost);
                return;
            }
        }

        // Pay Phyrexian mana first so colored mana is reserved for Phyrexian symbols
        // before generic costs consume it
        int phyrexianLifeCost = 0;
        if (cost.hasPhyrexianMana()) {
            phyrexianLifeCost = cost.payPhyrexianMana(pool, phyrexianLifeCount);
        }

        if (!convokeContributions.isEmpty()) {
            cost.payWithConvoke(pool, additionalCost, convokeContributions);
        } else if (cost.hasX() && card.getXColorRestriction() != null) {
            cost.pay(pool, effectiveXValue, card.getXColorRestriction(), additionalCost);
        } else if (cost.hasX()) {
            if (flags.hasRestricted()) {
                cost.pay(pool, effectiveXValue + additionalCost, flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(), flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless());
            } else {
                cost.pay(pool, effectiveXValue + additionalCost);
            }
        } else {
            if (flags.hasRestricted()) {
                cost.pay(pool, additionalCost, flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext(), flags.kickedOnlyGreen(), flags.instantSorceryOnlyColorless());
            } else {
                cost.pay(pool, additionalCost);
            }
        }

        if (phyrexianLifeCost > 0) {
            int currentLife = gameData.getLife(playerId);
            gameData.playerLifeTotals.put(playerId, currentLife - phyrexianLifeCost);
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " pays " + phyrexianLifeCost + " life for Phyrexian mana.");
        }
    }

    private DealDividedDamageAmongAnyTargetsEffect findKickedDividedDamageEffect(List<CardEffect> effects) {
        for (CardEffect e : effects) {
            if (e instanceof KickerReplacementEffect kre
                    && kre.kickedEffect() instanceof DealDividedDamageAmongAnyTargetsEffect ddae) {
                return ddae;
            }
        }
        return null;
    }

    private KickerEffect findKickerEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof KickerEffect)
                .map(e -> (KickerEffect) e)
                .findFirst().orElse(null);
    }

    private void payKickerCost(GameData gameData, Player player, Card card, KickerEffect kickerEffect, UUID sacrificePermanentId) {
        UUID playerId = player.getId();

        // Pay mana cost if any
        if (kickerEffect.hasManaCost()) {
            ManaCost kickerCost = new ManaCost(kickerEffect.cost());
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (pool.getKickedOnlyGreen() > 0) {
                if (!kickerCost.canPay(pool, 0, false, false, false, true)) {
                    throw new IllegalStateException("Not enough mana to pay kicker cost");
                }
                kickerCost.pay(pool, 0, false, false, false, true);
            } else {
                if (!kickerCost.canPay(pool)) {
                    throw new IllegalStateException("Not enough mana to pay kicker cost");
                }
                kickerCost.pay(pool, 0);
            }
        }

        // Pay sacrifice cost if any
        if (kickerEffect.hasSacrificeCost()) {
            paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    kickerEffect.sacrificeDescription(),
                    p -> gameQueryService.matchesPermanentPredicate(gameData, p, kickerEffect.sacrificePredicate()));
        }
    }

    private void payAlternateCastingCost(GameData gameData, Player player, Card card, List<UUID> sacrificePermanentIds) {
        AlternateHandCast altCast = card.getCastingOption(AlternateHandCast.class)
                .orElseThrow(() -> new IllegalStateException("Card does not have an alternate casting cost"));
        UUID playerId = player.getId();

        // Sacrifice all required permanents
        if (altCast.getCost(SacrificePermanentsCost.class).isPresent()) {
            for (UUID sacId : sacrificePermanentIds) {
                Permanent toSacrifice = gameQueryService.findPermanentById(gameData, sacId);
                if (toSacrifice != null && permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            player.getUsername() + " sacrifices " + toSacrifice.getCard().getName()
                                    + " for " + card.getName() + ".");
                }
            }
        }

        // Pay life
        altCast.getCost(LifeCastingCost.class).ifPresent(lifeCost -> {
            int currentLife = gameData.getLife(playerId);
            gameData.playerLifeTotals.put(playerId, currentLife - lifeCost.amount());
            gameBroadcastService.logAndBroadcast(gameData,
                    player.getUsername() + " pays " + lifeCost.amount() + " life for " + card.getName() + ".");
        });

        // Tap untapped permanents
        var tapCost = altCast.getCost(TapUntappedPermanentsCost.class);
        if (tapCost.isPresent()) {
            int sacCount = altCast.getCost(SacrificePermanentsCost.class).map(SacrificePermanentsCost::count).orElse(0);
            List<UUID> tapIds = sacrificePermanentIds.subList(sacCount, sacrificePermanentIds.size());
            for (UUID tapId : tapIds) {
                Permanent toTap = gameQueryService.findPermanentById(gameData, tapId);
                if (toTap != null) {
                    toTap.tap();
                    gameBroadcastService.logAndBroadcast(gameData,
                            player.getUsername() + " taps " + toTap.getCard().getName()
                                    + " for " + card.getName() + ".");
                }
            }
        }

        // Pay mana (for alternate costs that include a mana component)
        altCast.getCost(ManaCastingCost.class).ifPresent(manaCost -> {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            ManaCost cost = new ManaCost(manaCost.manaCost());
            cost.pay(pool);
            gameBroadcastService.logAndBroadcast(gameData,
                    player.getUsername() + " pays " + manaCost.manaCost() + " for " + card.getName() + ".");
        });
    }

    public void finishSpellCast(GameData gameData, UUID playerId, Player player, List<Card> hand, Card card) {
        finishSpellCast(gameData, playerId, player, hand, card, true);
    }

    public void finishSpellCast(GameData gameData, UUID playerId, Player player, List<Card> hand, Card card, boolean castFromHand) {
        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        String logEntry = player.getUsername() + " casts " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, castFromHand);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

}
