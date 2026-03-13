package com.github.laxika.magicalvibes.service.spell;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.model.AlternateCastingCost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    private record ManaRestrictionFlags(boolean isArtifact, boolean isMyr, boolean hasRestrictedRedContext) {
        boolean hasRestricted() { return isArtifact || isMyr || hasRestrictedRedContext; }
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
                    card.setTargetFilter(chosen.targetFilter());
                }
                return 0;
            }
        }
        return effectiveXValue;
    }

    private ManaRestrictionFlags computeManaRestrictionFlags(Card card) {
        boolean isArtifact = card.getType() == CardType.ARTIFACT
                || card.getAdditionalTypes().contains(CardType.ARTIFACT);
        boolean isMyr = card.getSubtypes().contains(CardSubtype.MYR);
        boolean hasRestrictedRedContext = isArtifact || card.getType() == CardType.CREATURE;
        return new ManaRestrictionFlags(isArtifact, isMyr, hasRestrictedRedContext);
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

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId) {
        playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, null, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount) {
        playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                  Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (targetPermanentIds == null) targetPermanentIds = List.of();
        if (convokeCreatureIds == null) convokeCreatureIds = List.of();
        if (alternateCostSacrificePermanentIds == null) alternateCostSacrificePermanentIds = List.of();
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();
        boolean usingAlternateCost = !alternateCostSacrificePermanentIds.isEmpty();

        List<Card> handEarly = gameData.playerHands.get(playerId);
        if (!fromGraveyard && (cardIndex < 0 || cardIndex >= handEarly.size())) {
            throw new IllegalArgumentException("Invalid card index");
        }

        // Handle playing a land from graveyard (e.g. via Crucible of Worlds)
        if (fromGraveyard) {
            List<Integer> playableGraveyard = gameBroadcastService.getPlayableGraveyardLandIndices(gameData, playerId);
            if (!playableGraveyard.contains(cardIndex)) {
                throw new IllegalStateException("Card is not playable from graveyard");
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            Card graveyardCard = graveyard.get(cardIndex);
            if (graveyardCard.getType() != CardType.LAND) {
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
            if (usingAlternateCost && cardCheck.getAlternateCastingCost() != null) {
                // Allow — alternate cost bypasses mana check; validated below
            } else if (cardCheck.getKeywords().contains(Keyword.CONVOKE) && !convokeCreatureIds.isEmpty()) {
                // Allow convoke-assisted casting even if not in basic playable list
                List<Integer> convokePlayable = gameBroadcastService.getPlayableCardIndices(gameData, playerId, convokeCreatureIds.size());
                if (!convokePlayable.contains(cardIndex)) {
                    throw new IllegalStateException("Card is not playable even with convoke");
                }
            } else {
                throw new IllegalStateException("Card is not playable");
            }
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.get(cardIndex);
        List<CardEffect> filteredSpellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        SacrificeCostFlags sacFlags = extractAndRemoveSacrificeCosts(filteredSpellEffects);

        // Handle modal spells (Choose one): unwrap at cast time per MTG CR 700.2a
        boolean wasModal = filteredSpellEffects.stream().anyMatch(ChooseOneEffect.class::isInstance);
        effectiveXValue = unwrapChooseOneEffect(card, filteredSpellEffects, effectiveXValue);

        // For modal spells, derive targeting from the chosen mode's unwrapped effect;
        // for non-modal spells, use the card's declared targeting (which accounts for auras, ETB effects, etc.)
        boolean unwrappedNeedsSpellTarget = wasModal
                ? filteredSpellEffects.stream().anyMatch(CardEffect::canTargetSpell)
                : card.isNeedsSpellTarget();
        boolean unwrappedNeedsTarget = wasModal
                ? filteredSpellEffects.stream().anyMatch(e -> e.canTargetPermanent() || e.canTargetPlayer() || e.canTargetGraveyard())
                : card.isNeedsTarget();

        // Validate alternate casting cost if used (e.g. Demon of Death's Gate)
        if (usingAlternateCost) {
            AlternateCastingCost altCost = card.getAlternateCastingCost();
            if (altCost == null) {
                throw new IllegalStateException("Card does not have an alternate casting cost");
            }
            if (alternateCostSacrificePermanentIds.size() != altCost.sacrificeCount()) {
                throw new IllegalStateException("Must sacrifice exactly " + altCost.sacrificeCount() + " permanents");
            }
            int currentLife = gameData.getLife(playerId);
            if (currentLife < altCost.lifeCost()) {
                throw new IllegalStateException("Not enough life to pay alternate cost");
            }
            // Validate each sacrifice target
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            for (UUID sacId : alternateCostSacrificePermanentIds) {
                Permanent toSacrifice = battlefield.stream()
                        .filter(p -> p.getId().equals(sacId))
                        .findFirst()
                        .orElse(null);
                if (toSacrifice == null) {
                    throw new IllegalStateException("Sacrifice target not found on your battlefield");
                }
                if (!gameQueryService.matchesPermanentPredicate(gameData, toSacrifice, altCost.sacrificeFilter())) {
                    throw new IllegalStateException("Sacrifice target does not match the required filter");
                }
            }
        }

        if (!usingAlternateCost) {
            // For X-cost spells, validate that player can pay colored + generic + xValue + any cost increases
            if (card.getManaCost() != null) {
                ManaCost cost = new ManaCost(card.getManaCost());
                if (cost.hasX()) {
                    if (effectiveXValue < 0) {
                        throw new IllegalStateException("X value cannot be negative");
                    }
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    int additionalCost = gameBroadcastService.getCastCostModifier(gameData, playerId, card);
                    int perTargetCost = card.getAdditionalCostPerExtraTarget() * Math.max(0, targetPermanentIds.size() - 1);
                    additionalCost += perTargetCost;
                    ManaRestrictionFlags flags = computeManaRestrictionFlags(card);
                    if (card.getXColorRestriction() != null) {
                        if (!cost.canPay(pool, effectiveXValue, card.getXColorRestriction(), additionalCost)) {
                            throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                        }
                    } else if (flags.hasRestricted()) {
                        if (!cost.canPay(pool, effectiveXValue + additionalCost, flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext())) {
                            throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                        }
                    } else if (!cost.canPay(pool, effectiveXValue + additionalCost)) {
                        throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                    }
                }
            }

            // Validate creature-only mana restriction (e.g. Myr Superion)
            if (card.isRequiresCreatureMana()) {
                ManaCost creatureCost = new ManaCost(card.getManaCost());
                ManaPool creaturePool = gameData.playerManaPools.get(playerId);
                int additionalCostForCreature = gameBroadcastService.getCastCostModifier(gameData, playerId, card);
                if (!creatureCost.canPayCreatureOnly(creaturePool, additionalCostForCreature)) {
                    throw new IllegalStateException("Can only spend mana produced by creatures to cast this spell");
                }
            }
        }

        // Validate spell target (targeting a spell on the stack)
        if (unwrappedNeedsSpellTarget) {
            targetLegalityService.validateSpellTargetOnStack(gameData, targetPermanentId, card.getTargetFilter(), playerId);
        }

        ReturnCardFromGraveyardEffect graveyardReturnEffect = (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.SPELL).stream()
                .filter(e -> e instanceof ReturnCardFromGraveyardEffect)
                .findFirst().orElse(null);
        boolean needsSingleGraveyardTargeting = graveyardReturnEffect != null;

        // Detect any effect that targets a graveyard card (e.g. PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect)
        boolean needsGraveyardEffectTargeting = !needsSingleGraveyardTargeting
                && card.getEffects(EffectSlot.SPELL).stream().anyMatch(e -> e.canTargetGraveyard());
        boolean canTargetAnyGraveyard = card.getEffects(EffectSlot.SPELL).stream().anyMatch(e -> e.canTargetAnyGraveyard());

        // Validate target if specified (can be a permanent or a player)
        if (targetPermanentId != null && !unwrappedNeedsSpellTarget) {
            if (needsSingleGraveyardTargeting) {
                String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
                if (graveyardReturnEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
                    boolean inControllersGraveyard = gameData.playerGraveyards
                            .getOrDefault(playerId, List.of())
                            .stream()
                            .anyMatch(c -> c.getId().equals(targetPermanentId));
                    if (!inControllersGraveyard) {
                        throw new IllegalStateException("Target must be a " + filterLabel + " in your graveyard");
                    }
                }
                targetLegalityService.validateEffectTargetInZone(gameData, card, targetPermanentId, Zone.GRAVEYARD, effectiveXValue);
            } else if (needsGraveyardEffectTargeting) {
                if (!canTargetAnyGraveyard) {
                    boolean inControllersGraveyard = gameData.playerGraveyards
                            .getOrDefault(playerId, List.of())
                            .stream()
                            .anyMatch(c -> c.getId().equals(targetPermanentId));
                    if (inControllersGraveyard) {
                        throw new IllegalStateException("Target must be in an opponent's graveyard");
                    }
                }
                targetLegalityService.validateEffectTargetInZone(gameData, card, targetPermanentId, Zone.GRAVEYARD);
            } else {
                targetLegalityService.validateSpellTargeting(gameData, card, targetPermanentId, null, playerId, unwrappedNeedsTarget);
            }
        } else if (unwrappedNeedsTarget && needsSingleGraveyardTargeting) {
            String filterLabel = CardPredicateUtils.describeFilter(graveyardReturnEffect.filter());
            throw new IllegalStateException("Must target a " + filterLabel + " in your graveyard");
        } else if (unwrappedNeedsTarget && needsGraveyardEffectTargeting) {
            throw new IllegalStateException("Must target a card in a graveyard");
        }

        // Validate multi-target permanent targeting
        if (card.getMaxTargets() > 0 && !targetPermanentIds.isEmpty()) {
            targetLegalityService.validateMultiSpellTargets(gameData, card, targetPermanentIds, playerId);
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
                    .filter(c -> c.getType() == CardType.CREATURE)
                    .count();
            if (effectiveXValue > creatureCount) {
                throw new IllegalStateException("Not enough creature cards in graveyard (need " + effectiveXValue + ", have " + creatureCount + ")");
            }
        }

        hand.remove(cardIndex);

        if (card.getType() == CardType.LAND) {
            // Lands bypass the stack — go directly onto battlefield
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, new Permanent(card));
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);

            String logEntry = player.getUsername() + " plays " + card.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} plays {}", gameData.id, player.getUsername(), card.getName());

            // Process ETB effects for lands (e.g. Glimmerpost)
            battlefieldEntryService.processCreatureETBEffects(gameData, playerId, card, null, false);

            turnProgressionService.resolveAutoPass(gameData);
        } else if (card.getType() == CardType.CREATURE || card.getType() == CardType.ENCHANTMENT
                || card.getType() == CardType.ARTIFACT || card.getType() == CardType.PLANESWALKER) {
            // Permanent spells: pay mana (or alternate cost), put on stack, finish
            int manaCostX = (card.getType() == CardType.ARTIFACT) ? effectiveXValue : 0;
            int stackX = (card.getType() == CardType.CREATURE || card.getType() == CardType.ARTIFACT)
                    ? effectiveXValue : 0;
            UUID stackTarget = (card.getType() == CardType.PLANESWALKER) ? null : targetPermanentId;

            if (usingAlternateCost) {
                payAlternateCastingCost(gameData, player, card, alternateCostSacrificePermanentIds);
            } else {
                paySpellManaCost(gameData, playerId, card, manaCostX, convokeContributions, phyrexianLifeCount);
            }
            gameData.stack.add(new StackEntry(
                    cardTypeToStackEntryType(card.getType()), card, playerId, card.getName(),
                    List.of(), stackX, stackTarget, null
            ));
            finishSpellCast(gameData, playerId, player, hand, card);
        } else if (card.getType() == CardType.SORCERY || card.getType() == CardType.INSTANT) {
            // Sorcery/Instant spells: pay mana + sacrifice costs, handle targeting, put on stack
            StackEntryType entryType = cardTypeToStackEntryType(card.getType());
            int resolvedXValue = effectiveXValue;
            int perTargetCost = card.getAdditionalCostPerExtraTarget() * Math.max(0, targetPermanentIds.size() - 1);
            if (usingAlternateCost) {
                payAlternateCastingCost(gameData, player, card, alternateCostSacrificePermanentIds);
            } else {
                paySpellManaCost(gameData, playerId, card, resolvedXValue + perTargetCost, convokeContributions, phyrexianLifeCount);
            }
            resolvedXValue = payAllSacrificeCosts(gameData, player, card, sacrificePermanentId, sacFlags, resolvedXValue);

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

            if (graveyardToHandEffect != null) {
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
            } else if (card.isNeedsDamageDistribution()) {
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
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, targetPermanentId, Zone.STACK
                ));
            } else if (!targetPermanentIds.isEmpty() && !sacFlags.usesSacrificeAllCreaturesCost()) {
                // Multi-target spell (e.g. "one or two target creatures each get +2/+1")
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetPermanentIds
                ));
            } else if (needsSingleGraveyardTargeting || needsGraveyardEffectTargeting) {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetPermanentId, null,
                        Map.of(), Zone.GRAVEYARD, List.of(), List.of()
                ));
            } else {
                gameData.stack.add(new StackEntry(
                        entryType, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetPermanentId, null
                ));
            }
            finishSpellCast(gameData, playerId, player, hand, card);
        }
    }

    // --- Sacrifice cost payment ---

    private int payAllSacrificeCosts(GameData gameData, Player player, Card card,
                                     UUID sacrificePermanentId, SacrificeCostFlags sacFlags, int resolvedXValue) {
        if (sacFlags.usesSacrificeCreatureCost()) {
            int sacrificedPower = paySingleSacrificeCost(gameData, player, card, sacrificePermanentId,
                    "a creature", p -> gameQueryService.isCreature(gameData, p));
            SacrificeCreatureCost sacCreatureCost = (SacrificeCreatureCost) card.getEffects(EffectSlot.SPELL).stream()
                    .filter(SacrificeCreatureCost.class::isInstance)
                    .findFirst().orElseThrow();
            if (sacCreatureCost.trackSacrificedPower()) {
                resolvedXValue = sacrificedPower;
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

    private int paySingleSacrificeCost(GameData gameData, Player player, Card sourceCard,
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
        if (permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    player.getUsername() + " sacrifices " + toSacrifice.getCard().getName()
                            + " for " + sourceCard.getName() + ".");
        }
        return power;
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

    // --- Play from exile ---

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue, UUID targetPermanentId) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();

        // Find the card in exile
        List<Card> exiledCards = gameData.playerExiledCards.get(playerId);
        if (exiledCards == null) {
            throw new IllegalStateException("No exiled cards");
        }

        UUID permittedPlayer = gameData.exilePlayPermissions.get(exileCardId);
        if (permittedPlayer == null || !permittedPlayer.equals(playerId)) {
            throw new IllegalStateException("No permission to play this exiled card");
        }

        Card card = null;
        int cardIndex = -1;
        for (int i = 0; i < exiledCards.size(); i++) {
            if (exiledCards.get(i).getId().equals(exileCardId)) {
                card = exiledCards.get(i);
                cardIndex = i;
                break;
            }
        }
        if (card == null) {
            throw new IllegalStateException("Card not found in exile");
        }

        // Remove from exile and clean up permission
        exiledCards.remove(cardIndex);
        gameData.exilePlayPermissions.remove(exileCardId);

        if (card.getType() == CardType.LAND) {
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
        if (card.getType() == CardType.SORCERY || card.getType() == CardType.INSTANT) {
            effectsToResolve = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
            extractAndRemoveSacrificeCosts(effectsToResolve);
            effectiveXValue = unwrapChooseOneEffect(card, effectsToResolve, effectiveXValue);
        } else {
            effectsToResolve = List.of();
        }

        gameData.stack.add(new StackEntry(
                entryType, card, playerId, card.getName(),
                effectsToResolve, effectiveXValue, targetPermanentId, null
        ));

        // Use null hand list — card was already removed from exile
        gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
        gameData.priorityPassedBy.clear();

        String logEntry = player.getUsername() + " casts " + card.getName() + " from exile.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} casts {} from exile", gameData.id, player.getUsername(), card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    // --- Mana payment ---

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions) {
        paySpellManaCost(gameData, playerId, card, effectiveXValue, convokeContributions, null);
    }

    public void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions, Integer phyrexianLifeCount) {
        if (card.getManaCost() == null) return;
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int additionalCost = gameBroadcastService.getCastCostModifier(gameData, playerId, card);
        ManaRestrictionFlags flags = computeManaRestrictionFlags(card);

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
                cost.pay(pool, effectiveXValue + additionalCost, flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext());
            } else {
                cost.pay(pool, effectiveXValue + additionalCost);
            }
        } else {
            if (flags.hasRestricted()) {
                cost.pay(pool, additionalCost, flags.isArtifact(), flags.isMyr(), flags.hasRestrictedRedContext());
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

    private void payAlternateCastingCost(GameData gameData, Player player, Card card, List<UUID> sacrificePermanentIds) {
        AlternateCastingCost altCost = card.getAlternateCastingCost();
        UUID playerId = player.getId();

        // Sacrifice all required permanents
        for (UUID sacId : sacrificePermanentIds) {
            Permanent toSacrifice = gameQueryService.findPermanentById(gameData, sacId);
            if (toSacrifice != null && permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        player.getUsername() + " sacrifices " + toSacrifice.getCard().getName()
                                + " for " + card.getName() + ".");
            }
        }

        // Pay life
        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - altCost.lifeCost());
        gameBroadcastService.logAndBroadcast(gameData,
                player.getUsername() + " pays " + altCost.lifeCost() + " life for " + card.getName() + ".");
    }

    public void finishSpellCast(GameData gameData, UUID playerId, Player player, List<Card> hand, Card card) {
        gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
        gameData.priorityPassedBy.clear();

        String logEntry = player.getUsername() + " casts " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

}
