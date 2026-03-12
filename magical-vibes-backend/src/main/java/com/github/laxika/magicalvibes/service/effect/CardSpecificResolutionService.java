package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.effect.w.WarpWorldEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.WarpWorldAuraChoiceRequest;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GenesisWaveEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.KarnRestartGameEffect;
import com.github.laxika.magicalvibes.model.effect.KothEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.VenserEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardSpecificResolutionService {

    private final GraveyardService graveyardService;
    private final WarpWorldService warpWorldService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final PermanentRemovalService permanentRemovalService;
    private final LegendRuleService legendRuleService;

    @HandlesEffect(WarpWorldEffect.class)
    void resolveWarpWorld(GameData gameData, StackEntry entry) {
        Map<UUID, Integer> permanentsOwnedByPlayer = new HashMap<>();
        Map<UUID, List<Card>> permanentsToShuffleByOwner = new HashMap<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            permanentsOwnedByPlayer.put(playerId, 0);
            permanentsToShuffleByOwner.put(playerId, new ArrayList<>());
        }

        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) {
                continue;
            }

            Iterator<Permanent> iterator = battlefield.iterator();
            while (iterator.hasNext()) {
                Permanent permanent = iterator.next();
                UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), controllerId);
                if (!permanentsOwnedByPlayer.containsKey(ownerId)) {
                    continue;
                }

                permanentsOwnedByPlayer.merge(ownerId, 1, Integer::sum);
                if (!permanent.getOriginalCard().isToken()) {
                    permanentsToShuffleByOwner.get(ownerId).add(permanent.getOriginalCard());
                }
                gameData.stolenCreatures.remove(permanent.getId());
                gameData.untilEndOfTurnStolenCreatures.remove(permanent.getId());
                gameData.enchantmentDependentStolenCreatures.remove(permanent.getId());
                gameData.permanentControlStolenCreatures.remove(permanent.getId());
                iterator.remove();
            }
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> ownedPermanents = permanentsToShuffleByOwner.get(playerId);
            if (ownedPermanents.isEmpty()) {
                continue;
            }

            List<Card> deck = gameData.playerDecks.get(playerId);
            deck.addAll(ownedPermanents);
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }

        Map<UUID, List<Card>> revealedByPlayer = new HashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            int ownedPermanentCount = permanentsOwnedByPlayer.getOrDefault(playerId, 0);
            List<Card> deck = gameData.playerDecks.get(playerId);
            int revealCount = Math.min(ownedPermanentCount, deck.size());

            List<Card> revealed = new ArrayList<>();
            for (int i = 0; i < revealCount; i++) {
                revealed.add(deck.removeFirst());
            }
            revealedByPlayer.put(playerId, revealed);
        }

        Map<UUID, List<Card>> putOntoBattlefieldByPlayer = new HashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            putOntoBattlefieldByPlayer.put(playerId, new ArrayList<>());
        }

        // Snapshot replacement effects from permanents that existed before this Warp World event.
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

        // First, put artifact/creature/land cards onto the battlefield.
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);

            for (Card card : revealed) {
                CardType type = card.getType();
                if (type == CardType.ARTIFACT || type == CardType.CREATURE || type == CardType.LAND) {
                    Permanent permanent = new Permanent(card);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent, enterTappedTypesSnapshot);
                    putOntoBattlefieldByPlayer.get(playerId).add(card);
                }
            }
        }

        List<UUID> auraLegalBaseTargetIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                auraLegalBaseTargetIds.addAll(battlefield.stream().map(Permanent::getId).toList())
        );

        List<UUID> choiceOrder = getApnapOrder(gameData);

        // Then, put enchantment cards onto the battlefield.
        gameData.warpWorldOperation.pendingAuraChoices.clear();
        gameData.warpWorldOperation.pendingEnchantmentPlacements.clear();
        for (UUID playerId : choiceOrder) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());

            for (Card card : revealed) {
                if (card.getType() == CardType.ENCHANTMENT) {
                    if (card.isAura()) {
                        List<UUID> validTargets = findLegalAuraAttachments(gameData, card, playerId, auraLegalBaseTargetIds);
                        if (validTargets.size() == 1) {
                            UUID attachmentTargetId = validTargets.getFirst();
                            gameData.warpWorldOperation.pendingEnchantmentPlacements.add(
                                    new WarpWorldEnchantmentPlacement(playerId, card, attachmentTargetId)
                            );
                            putOntoBattlefieldByPlayer.get(playerId).add(card);
                        } else if (!validTargets.isEmpty()) {
                            gameData.warpWorldOperation.pendingAuraChoices.addLast(
                                    new WarpWorldAuraChoiceRequest(playerId, card, validTargets)
                            );
                            // Will be put onto battlefield after choosing what it enchants.
                            putOntoBattlefieldByPlayer.get(playerId).add(card);
                        }
                    } else {
                        gameData.warpWorldOperation.pendingEnchantmentPlacements.add(
                                new WarpWorldEnchantmentPlacement(playerId, card, null)
                        );
                        putOntoBattlefieldByPlayer.get(playerId).add(card);
                    }
                }
            }
        }

        // Put the rest on the bottom of each player's library (in chosen order).
        gameData.pendingLibraryBottomReorders.clear();
        for (UUID playerId : choiceOrder) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());
            List<Card> deck = gameData.playerDecks.get(playerId);
            List<Card> putCards = putOntoBattlefieldByPlayer.get(playerId);
            List<Card> remaining = new ArrayList<>();

            for (Card card : revealed) {
                if (!putCards.contains(card)) {
                    remaining.add(card);
                }
            }

            if (remaining.size() <= 1) {
                deck.addAll(remaining);
            } else {
                gameData.pendingLibraryBottomReorders.addLast(new LibraryBottomReorderRequest(playerId, remaining));
            }
        }

        // Save post-resolution work until bottom-order choices are complete.
        gameData.warpWorldOperation.pendingCreaturesByPlayer.clear();
        gameData.warpWorldOperation.enterTappedTypesSnapshot.clear();
        gameData.warpWorldOperation.enterTappedTypesSnapshot.addAll(enterTappedTypesSnapshot);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> creatures = putOntoBattlefieldByPlayer.get(playerId).stream()
                    .filter(card -> card.getType() == CardType.CREATURE)
                    .toList();
            gameData.warpWorldOperation.pendingCreaturesByPlayer.put(playerId, new ArrayList<>(creatures));
        }
        gameData.warpWorldOperation.needsLegendChecks = true;
        gameData.warpWorldOperation.sourceName = entry.getCard().getName();

        if (!gameData.warpWorldOperation.pendingAuraChoices.isEmpty()) {
            warpWorldService.beginNextPendingWarpWorldAuraChoice(gameData);
            return;
        }
        warpWorldService.placePendingWarpWorldEnchantments(gameData);
        if (!gameData.pendingLibraryBottomReorders.isEmpty()) {
            warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
            return;
        }

        warpWorldService.finalizePendingWarpWorld(gameData);
    }

    @HandlesEffect(GenesisWaveEffect.class)
    void resolveGenesisWave(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        int xValue = entry.getXValue();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (xValue <= 0 || deck.isEmpty()) {
            String logMsg = playerName + " casts Genesis Wave with X=" + xValue + " — no cards revealed.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        int count = Math.min(xValue, deck.size());
        List<Card> revealedCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            boolean isPermanent = card.getType() != CardType.INSTANT
                    && card.getType() != CardType.SORCERY;
            if (isPermanent && card.getManaValue() <= xValue) {
                eligibleCards.add(card);
            }
        }

        String logMsg = playerName + " reveals the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        if (eligibleCards.isEmpty()) {
            for (Card card : revealedCards) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card);
            }
            String graveyardLog = playerName + " finds no eligible permanent cards. All revealed cards are put into their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, graveyardLog);
            return;
        }

        Set<UUID> validCardIds = ConcurrentHashMap.newKeySet();
        for (Card card : eligibleCards) {
            validCardIds.add(card.getId());
        }

        gameData.interaction.beginLibraryRevealChoice(controllerId, revealedCards, validCardIds, true);

        List<CardView> cardViews = eligibleCards.stream().map(cardViewFactory::create).toList();
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseMultipleCardsFromGraveyardsMessage(
                cardIds, cardViews, eligibleCards.size(),
                "Choose any number of permanent cards with mana value " + xValue + " or less to put onto the battlefield."
        ));

        log.info("Game {} - {} resolving Genesis Wave with X={}, {} revealed, {} eligible",
                gameData.id, playerName, xValue, count, eligibleCards.size());
    }

    @HandlesEffect(KothEmblemEffect.class)
    void resolveKothEmblem(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        ActivatedAbility mountainAbility = new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This land deals 1 damage to any target."
        );

        Emblem emblem = new Emblem(controllerId, List.of(
                new GrantActivatedAbilityEffect(mountainAbility, GrantScope.OWN_PERMANENTS,
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN))
        ), entry.getCard());

        gameData.emblems.add(emblem);

        String logEntry = playerName + " gets an emblem with \"Mountains you control have '{T}: This land deals 1 damage to any target.'\".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets Koth emblem", gameData.id, playerName);
    }

    @HandlesEffect(VenserEmblemEffect.class)
    void resolveVenserEmblem(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        Emblem emblem = new Emblem(controllerId, List.of(
                new ExileTargetOnControllerSpellCastEffect()
        ), entry.getCard());

        gameData.emblems.add(emblem);

        String logEntry = playerName + " gets an emblem with \"Whenever you cast a spell, exile target permanent.\".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets Venser emblem", gameData.id, playerName);
    }

    @HandlesEffect(SacrificeTargetThenRevealUntilTypeToBattlefieldEffect.class)
    void resolveSacrificeTargetThenRevealUntilTypeToBattlefield(GameData gameData, StackEntry entry,
                                                                 SacrificeTargetThenRevealUntilTypeToBattlefieldEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, entry.getTargetPermanentId());
        if (targetControllerId == null) {
            return;
        }

        String targetControllerName = gameData.playerIdToName.get(targetControllerId);
        String targetName = target.getCard().getName();

        // Sacrifice the targeted permanent
        permanentRemovalService.removePermanentToGraveyard(gameData, target);
        String sacrificeLog = targetControllerName + " sacrifices " + targetName + ".";
        gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);

        // Reveal cards from the top of the controller's library until a matching card is found
        List<Card> deck = gameData.playerDecks.get(targetControllerId);
        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;

        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (cardMatchesAnyType(card, effect.cardTypes())) {
                foundCard = card;
                break;
            }
        }

        if (revealedCards.isEmpty()) {
            String emptyLog = targetControllerName + "'s library is empty — no cards are revealed.";
            gameBroadcastService.logAndBroadcast(gameData, emptyLog);
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        String revealLog = targetControllerName + " reveals " + revealedNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);

        if (foundCard == null) {
            // No matching card found — shuffle all revealed cards back into the library
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetControllerId);
            String noMatchLog = targetControllerName + " reveals their entire library — no matching card found. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, noMatchLog);
            return;
        }

        // Put the found card onto the battlefield under the controller's control
        Permanent perm = new Permanent(foundCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetControllerId, perm);

        String enterLog = foundCard.getName() + " enters the battlefield under " + targetControllerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, enterLog);

        // Handle ETB effects for creatures
        boolean isCreature = foundCard.getType() == CardType.CREATURE
                || foundCard.getAdditionalTypes().contains(CardType.CREATURE);
        if (isCreature) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, targetControllerId, foundCard, null, false);
        }

        // Handle planeswalkers
        if (foundCard.getType() == CardType.PLANESWALKER && foundCard.getLoyalty() != null) {
            perm.setLoyaltyCounters(foundCard.getLoyalty());
            perm.setSummoningSick(false);
        }

        // Shuffle all other revealed cards back into the library
        revealedCards.remove(foundCard);
        if (!revealedCards.isEmpty()) {
            deck.addAll(revealedCards);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, targetControllerId);

        String shuffleLog = targetControllerName + " shuffles their library.";
        gameBroadcastService.logAndBroadcast(gameData, shuffleLog);

        // Check legend rule
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, targetControllerId);
        }

        log.info("Game {} - {} sacrificed {}, {} enters the battlefield",
                gameData.id, targetControllerName, targetName, foundCard.getName());
    }

    @HandlesEffect(ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect.class)
    void resolveExileAllCreaturesYouControlThenRevealCreaturesToBattlefield(GameData gameData, StackEntry entry,
                                                                            ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Step 1: Find all creatures the controller controls
        List<Permanent> creaturesToExile = new ArrayList<>(
                gameData.playerBattlefields.get(controllerId).stream()
                        .filter(p -> p.getCard().getType() == CardType.CREATURE
                                || p.getCard().getAdditionalTypes().contains(CardType.CREATURE))
                        .toList()
        );

        int creatureCount = creaturesToExile.size();

        if (creatureCount == 0) {
            String noCreaturesLog = controllerName + " controls no creatures — no cards are exiled or revealed.";
            gameBroadcastService.logAndBroadcast(gameData, noCreaturesLog);
            return;
        }

        // Step 2: Exile all creatures
        for (Permanent creature : creaturesToExile) {
            String creatureName = creature.getCard().getName();
            permanentRemovalService.removePermanentToExile(gameData, creature);
            String exileLog = controllerName + " exiles " + creatureName + ".";
            gameBroadcastService.logAndBroadcast(gameData, exileLog);
        }

        // Step 3: Reveal cards from the top of the library until finding that many creature cards
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> revealedCards = new ArrayList<>();
        List<Card> foundCreatures = new ArrayList<>();

        while (!deck.isEmpty() && foundCreatures.size() < creatureCount) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (cardMatchesAnyType(card, Set.of(CardType.CREATURE))) {
                foundCreatures.add(card);
            }
        }

        if (revealedCards.isEmpty()) {
            String emptyLog = controllerName + "'s library is empty — no cards are revealed.";
            gameBroadcastService.logAndBroadcast(gameData, emptyLog);
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        String revealLog = controllerName + " reveals " + revealedNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);

        if (foundCreatures.isEmpty()) {
            // No creature cards found — shuffle all revealed cards back into the library
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String noMatchLog = controllerName + " reveals their entire library — no creature cards found. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, noMatchLog);
            return;
        }

        // Step 4: Put all found creature cards onto the battlefield simultaneously (ruling 2010-08-15)
        for (Card creatureCard : foundCreatures) {
            Permanent perm = new Permanent(creatureCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

            String enterLog = creatureCard.getName() + " enters the battlefield under " + controllerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, enterLog);

            // Handle planeswalkers (e.g. artifact creatures that are also planeswalkers)
            if (creatureCard.getType() == CardType.PLANESWALKER && creatureCard.getLoyalty() != null) {
                perm.setLoyaltyCounters(creatureCard.getLoyalty());
                perm.setSummoningSick(false);
            }
        }

        // Step 5: Shuffle all non-creature revealed cards back into the library
        // This happens before ETB triggers are put on the stack (ruling 2010-08-15:
        // "Any abilities that trigger during the resolution of Mass Polymorph will wait
        // to be put onto the stack until Mass Polymorph finishes resolving.")
        List<Card> nonCreatureCards = new ArrayList<>(revealedCards);
        nonCreatureCards.removeAll(foundCreatures);
        if (!nonCreatureCards.isEmpty()) {
            deck.addAll(nonCreatureCards);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        String shuffleLog = controllerName + " shuffles their library.";
        gameBroadcastService.logAndBroadcast(gameData, shuffleLog);

        // Step 6: Process ETB triggers after all creatures are on the battlefield and the
        // spell has finished resolving. All creatures enter at the same time, so triggers
        // see the full board state.
        for (Card creatureCard : foundCreatures) {
            battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, creatureCard, null, false);
        }

        // Check legend rule for each creature that entered
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }

        log.info("Game {} - {} exiled {} creatures, {} creature cards entered the battlefield",
                gameData.id, controllerName, creatureCount, foundCreatures.size());
    }

    private boolean cardMatchesAnyType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) return true;
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (types.contains(additionalType)) return true;
        }
        return false;
    }

    private List<UUID> findLegalAuraAttachments(GameData gameData, Card auraCard, UUID auraControllerId, List<UUID> baseTargetIds) {
        List<UUID> validTargets = new ArrayList<>();
        gameData.forEachPermanent((playerId, candidate) -> {
            if (!baseTargetIds.contains(candidate.getId())) {
                return;
            }
            if (gameQueryService.hasProtectionFrom(gameData, candidate, auraCard.getColor())) {
                return;
            }
            if (gameQueryService.hasProtectionFromSourceCardTypes(candidate, auraCard)) {
                return;
            }
            if (gameQueryService.hasProtectionFromSourceSubtypes(candidate, auraCard)) {
                return;
            }
            if (auraCard.getTargetFilter() != null) {
                try {
                    gameQueryService.validateTargetFilter(auraCard.getTargetFilter(),
                            candidate,
                            FilterContext.of(gameData)
                                    .withSourceCardId(auraCard.getId())
                                    .withSourceControllerId(auraControllerId));
                } catch (IllegalStateException ignored) {
                    return;
                }
            }
            validTargets.add(candidate.getId());
        });

        return validTargets;
    }

    /**
     * Restarts the game per CR 726. Collects non-Aura permanent cards exiled with the source,
     * resets all game state, each player shuffles and draws 7, enters mulligan phase.
     * After mulligans, the saved cards enter the battlefield under the controller's control.
     * The controller goes first.
     */
    @HandlesEffect(KarnRestartGameEffect.class)
    void resolveKarnRestartGame(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Step 1: Collect non-Aura permanent cards exiled with Karn
        List<Card> karnExiledCards = new ArrayList<>();
        if (sourcePermanentId != null) {
            List<Card> pool = gameData.permanentExiledCards.get(sourcePermanentId);
            if (pool != null) {
                for (Card card : pool) {
                    boolean isAura = card.getSubtypes().contains(CardSubtype.AURA);
                    boolean isPermanent = card.getType() != CardType.INSTANT
                            && card.getType() != CardType.SORCERY;
                    if (isPermanent && !isAura) {
                        karnExiledCards.add(card);
                    }
                }
            }
        }

        String logEntry = controllerName + " restarts the game with Karn Liberated!";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} restarts the game with Karn Liberated ({} cards saved)",
                gameData.id, controllerName, karnExiledCards.size());

        // Step 2: Gather all non-token cards from all zones for each player into their library.
        // First, collect owned battlefield cards for each player (must happen before clearing
        // battlefields so stolen creature lookup works across all controllers).
        Map<UUID, List<Card>> ownedBattlefieldCards = new HashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> owned = new ArrayList<>();
            for (UUID ctrlId : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(ctrlId);
                if (bf == null) continue;
                for (Permanent perm : bf) {
                    if (!perm.getCard().isToken()) {
                        UUID ownerId = gameData.stolenCreatures.getOrDefault(perm.getId(), ctrlId);
                        if (ownerId.equals(playerId)) {
                            owned.add(perm.getOriginalCard());
                        }
                    }
                }
            }
            ownedBattlefieldCards.put(playerId, owned);
        }

        // Clear all battlefields now that ownership has been resolved
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf != null) bf.clear();
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> allCards = new ArrayList<>();

            // From deck
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null) {
                allCards.addAll(deck);
                deck.clear();
            }

            // From hand
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand != null) {
                allCards.addAll(hand);
                hand.clear();
            }

            // From graveyard
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                allCards.addAll(graveyard);
                graveyard.clear();
            }

            // From exile
            List<Card> exile = gameData.playerExiledCards.get(playerId);
            if (exile != null) {
                // Remove cards that Karn is keeping
                exile.removeAll(karnExiledCards);
                allCards.addAll(exile);
                exile.clear();
            }

            // From battlefield (already collected above)
            allCards.addAll(ownedBattlefieldCards.get(playerId));

            // Remove Karn's saved cards from the pool (they stay in exile)
            allCards.removeAll(karnExiledCards);

            // Shuffle into library
            Collections.shuffle(allCards);
            deck = gameData.playerDecks.get(playerId);
            if (deck == null) {
                deck = Collections.synchronizedList(new ArrayList<>());
                gameData.playerDecks.put(playerId, deck);
            }
            deck.addAll(allCards);
        }

        // Step 3: Clear all game state
        gameData.stack.clear();
        gameData.permanentExiledCards.clear();
        gameData.stolenCreatures.clear();
        gameData.untilEndOfTurnStolenCreatures.clear();
        gameData.enchantmentDependentStolenCreatures.clear();
        gameData.permanentControlStolenCreatures.clear();
        gameData.pendingExileReturns.clear();
        gameData.exileReturnOnPermanentLeave.clear();
        gameData.sourceLinkedAnimations.clear();
        gameData.pendingTokenExilesAtEndStep.clear();
        gameData.pendingMayAbilities.clear();
        gameData.pendingDeathTriggerTargets.clear();
        gameData.pendingDiscardSelfTriggers.clear();
        gameData.pendingAttackTriggerTargets.clear();
        gameData.pendingSpellTargetTriggers.clear();
        gameData.pendingEmblemTriggerTargets.clear();
        gameData.pendingUpkeepCopyTargets.clear();
        gameData.pendingEachPlayerDiscardQueue.clear();
        gameData.emblems.clear();
        gameData.extraTurns.clear();
        gameData.pendingLibraryBottomReorders.clear();
        gameData.openingHandRevealTriggers.clear();
        gameData.openingHandManaTriggers.clear();
        gameData.playersWhoCastFirstSpellInGame.clear();
        gameData.playersWithNoMaximumHandSize.clear();
        gameData.priorityPassedBy.clear();
        gameData.permanentsToSacrificeAtEndOfCombat.clear();
        gameData.permanentsPreventedFromDealingDamage.clear();
        gameData.drawReplacementTargetToController.clear();
        gameData.playerSpellsCantBeCounteredByColorsThisTurn.clear();
        gameData.playerCreaturesCantBeTargetedByColorsThisTurn.clear();
        gameData.activatedAbilityUsesThisTurn.clear();
        gameData.pendingTurnControl.clear();
        gameData.combatDamageToPlayersThisTurn.clear();
        gameData.paidSearchTaxPermanentIds.clear();
        gameData.combatDamagePlayerAssignments.clear();
        gameData.combatDamagePendingIndices.clear();
        gameData.playersAttemptedDrawFromEmptyLibrary.clear();
        gameData.preventDamageFromColors.clear();

        gameData.pendingEffectResolutionEntry = null;
        gameData.pendingEffectResolutionIndex = 0;
        gameData.chosenXValue = null;
        gameData.pendingAbilityActivation = null;
        gameData.knowledgePoolSourcePermanentId = null;
        gameData.imprintSourcePermanentId = null;
        gameData.pendingOpponentExileChoice = null;
        gameData.pendingCombatDamageBounceTargetPlayerId = null;
        gameData.pendingSacrificeSelfToDestroySourceId = null;
        gameData.pendingProliferateCount = 0;
        gameData.pendingReturnToHandOnDiscardType = null;
        gameData.pendingEachPlayerDiscardControllerId = null;
        gameData.pendingEachPlayerDiscardAmount = 0;
        gameData.combatDamageRedirectTarget = null;
        gameData.globalDamagePreventionShield = 0;
        gameData.preventAllCombatDamage = false;
        gameData.allPermanentsEnterTappedThisTurn = false;
        gameData.endTurnRequested = false;
        gameData.additionalCombatMainPhasePairs = 0;
        gameData.discardCausedByOpponent = false;
        gameData.cleanupDiscardPending = false;
        gameData.pendingSacrificeAttackingCreature = false;
        gameData.pendingAwakeningCounterPlacement = false;
        gameData.pendingTapSubtypeBoostSourcePermanentId = null;
        gameData.mindControlledPlayerId = null;
        gameData.mindControllerPlayerId = null;
        gameData.pendingSearchContext = null;
        gameData.pendingETBDamageAssignments = Map.of();
        gameData.combatDamagePhase1Complete = false;
        gameData.combatDamagePhase1State = null;

        for (UUID playerId : gameData.orderedPlayerIds) {
            gameData.playerLifeTotals.put(playerId, 20);
            gameData.playerPoisonCounters.put(playerId, 0);
            gameData.playerManaPools.put(playerId, new ManaPool());
            gameData.landsPlayedThisTurn.put(playerId, 0);
            gameData.spellsCastThisTurn.put(playerId, 0);
            gameData.cardsDrawnThisTurn.put(playerId, 0);
            gameData.playerDamagePreventionShields.put(playerId, 0);
            gameData.permanentsEnteredBattlefieldThisTurn.put(playerId, Collections.synchronizedList(new ArrayList<>()));
            gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.put(playerId, ConcurrentHashMap.newKeySet());
            gameData.creatureDeathCountThisTurn.put(playerId, 0);
            gameData.creatureCardsDamagedThisTurnBySourcePermanent.put(playerId, ConcurrentHashMap.newKeySet());
            gameData.creatureGivingControllerPoisonOnDeathThisTurn.clear();
            gameData.playerSourceDamagePreventionIds.put(playerId, ConcurrentHashMap.newKeySet());
            gameData.playerColorDamagePreventionCount.put(playerId, new ConcurrentHashMap<>());

            if (gameData.playerBattlefields.get(playerId) == null) {
                gameData.playerBattlefields.put(playerId, Collections.synchronizedList(new ArrayList<>()));
            }
        }

        gameData.interaction.clearAwaitingInput();
        gameData.turnNumber = 1;

        // Step 4: Each player draws 7 cards (CR 726 — pregame procedure)
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            List<Card> hand = gameData.playerHands.get(playerId);
            int toDraw = Math.min(7, deck.size());
            for (int i = 0; i < toDraw; i++) {
                hand.add(deck.removeFirst());
            }
            String drawLog = gameData.playerIdToName.get(playerId) + " draws 7 cards.";
            gameBroadcastService.logAndBroadcast(gameData, drawLog);
        }

        // Step 5: Controller goes first (CR 726)
        gameData.startingPlayerId = controllerId;

        // Step 6: Enter mulligan phase. After mulligans complete, Karn's exiled cards
        // will be put onto the battlefield (handled by MulliganService.startGame).
        gameData.pendingKarnRestartCards = karnExiledCards;
        gameData.karnRestartControllerId = controllerId;

        for (UUID playerId : gameData.orderedPlayerIds) {
            gameData.mulliganCounts.put(playerId, 0);
        }
        gameData.playerKeptHand.clear();
        gameData.playerNeedsToBottom.clear();
        gameData.status = GameStatus.MULLIGAN;

        gameBroadcastService.logAndBroadcast(gameData, "Mulligan phase — decide to keep or mulligan.");
        gameBroadcastService.broadcastGameState(gameData);
    }

    private List<UUID> getApnapOrder(GameData gameData) {
        if (gameData.activePlayerId == null || !gameData.orderedPlayerIds.contains(gameData.activePlayerId)) {
            return new ArrayList<>(gameData.orderedPlayerIds);
        }

        int activeIndex = gameData.orderedPlayerIds.indexOf(gameData.activePlayerId);
        List<UUID> apnap = new ArrayList<>(gameData.orderedPlayerIds.size());
        for (int i = 0; i < gameData.orderedPlayerIds.size(); i++) {
            int idx = (activeIndex + i) % gameData.orderedPlayerIds.size();
            apnap.add(gameData.orderedPlayerIds.get(idx));
        }
        return apnap;
    }
}

