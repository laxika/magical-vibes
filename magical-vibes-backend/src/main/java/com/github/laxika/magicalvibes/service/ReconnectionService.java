package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.model.CombatDamageTargetView;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ScryMessage;
import com.github.laxika.magicalvibes.networking.message.XValueChoiceMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconnectionService {

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST, CardSubtype.MOUNTAIN, CardSubtype.ISLAND,
            CardSubtype.PLAINS, CardSubtype.SWAMP, CardSubtype.AURA,
            CardSubtype.EQUIPMENT, CardSubtype.LOCUS
    );

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final CombatService combatService;
    private final GameQueryService gameQueryService;

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        InteractionContext context = gameData.interaction.currentContext();
        if (context != null) {
            resendFromContext(gameData, playerId, context);
            return;
        }
        AwaitingInput awaitingInput = gameData.interaction.awaitingInputType();
        if (awaitingInput == null) return;

        switch (awaitingInput) {
            case ATTACKER_DECLARATION -> {
                if (playerId.equals(gameData.activePlayerId)) {
                    List<Integer> attackable = combatService.getAttackableCreatureIndices(gameData, playerId);
                    List<Integer> mustAttack = combatService.getMustAttackIndices(gameData, playerId, attackable);
                    sessionManager.sendToPlayer(playerId, new AvailableAttackersMessage(attackable, mustAttack));
                }
            }
            case BLOCKER_DECLARATION -> {
                UUID defenderId = gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
                if (playerId.equals(defenderId)) {
                    List<Integer> blockable = combatService.getBlockableCreatureIndices(gameData, defenderId);
                    List<Integer> attackerIndices = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
                    List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
                    attackerIndices = attackerIndices.stream()
                            .filter(idx -> !gameQueryService.hasCantBeBlocked(gameData, attackerBattlefield.get(idx)))
                            .toList();
                    var legalPairs = combatService.computeLegalBlockPairs(gameData, blockable, attackerIndices, defenderId, gameData.activePlayerId);
                    sessionManager.sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices, legalPairs));
                }
            }
            case CARD_CHOICE, TARGETED_CARD_CHOICE, DISCARD_CHOICE, ACTIVATED_ABILITY_DISCARD_COST_CHOICE -> {
                InteractionContext.CardChoice cc = gameData.interaction.cardChoiceContext();
                if (cc != null) {
                    resendFromContext(gameData, playerId, cc);
                }
            }
            case PERMANENT_CHOICE -> {
                InteractionContext.PermanentChoice pc = gameData.interaction.permanentChoiceContextView();
                if (pc != null) {
                    resendFromContext(gameData, playerId, pc);
                }
            }
            case GRAVEYARD_CHOICE, ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE -> {
                InteractionContext.GraveyardChoice gc = gameData.interaction.graveyardChoiceContext();
                if (gc != null) {
                    resendFromContext(gameData, playerId, gc);
                }
            }
            case COLOR_CHOICE -> {
                InteractionContext.ColorChoice cc = gameData.interaction.colorChoiceContextView();
                if (cc != null) {
                    resendFromContext(gameData, playerId, cc);
                }
            }
            case MAY_ABILITY_CHOICE -> {
                InteractionContext.MayAbilityChoice mc = gameData.interaction.mayAbilityChoiceContext();
                if (mc != null) {
                    // keep legacy description when context was not populated
                    if (mc.description().isBlank() && !gameData.pendingMayAbilities.isEmpty()) {
                        resendFromContext(gameData, playerId, new InteractionContext.MayAbilityChoice(mc.playerId(),
                                gameData.pendingMayAbilities.getFirst().description()));
                    } else {
                        resendFromContext(gameData, playerId, mc);
                    }
                }
            }
            case MULTI_PERMANENT_CHOICE -> {
                InteractionContext.MultiPermanentChoice mpc = gameData.interaction.multiPermanentChoiceContext();
                if (mpc != null) {
                    resendFromContext(gameData, playerId, mpc);
                }
            }
            case MULTI_GRAVEYARD_CHOICE -> {
                InteractionContext.MultiGraveyardChoice mgc = gameData.interaction.multiGraveyardChoiceContext();
                if (mgc != null) {
                    resendFromContext(gameData, playerId, mgc);
                }
            }
            case LIBRARY_REORDER -> {
                InteractionContext.LibraryReorder lr = gameData.interaction.libraryReorderContext();
                if (lr != null) {
                    resendFromContext(gameData, playerId, lr);
                }
            }
            case LIBRARY_SEARCH -> {
                InteractionContext.LibrarySearch ls = gameData.interaction.librarySearchContext();
                if (ls != null) {
                    resendFromContext(gameData, playerId, ls);
                }
            }
            case LIBRARY_REVEAL_CHOICE -> {
                InteractionContext.LibraryRevealChoice lrc = gameData.interaction.libraryRevealChoiceContext();
                if (lrc != null) {
                    resendFromContext(gameData, playerId, lrc);
                }
            }
            case HAND_TOP_BOTTOM_CHOICE -> {
                InteractionContext.HandTopBottomChoice htbc = gameData.interaction.handTopBottomChoiceContext();
                if (htbc != null) {
                    resendFromContext(gameData, playerId, htbc);
                }
            }
            case REVEALED_HAND_CHOICE -> {
                InteractionContext.RevealedHandChoice rhc = gameData.interaction.revealedHandChoiceContext();
                if (rhc != null) {
                    resendFromContext(gameData, playerId, rhc);
                }
            }
            case MULTI_ZONE_EXILE_CHOICE -> {
                InteractionContext.MultiZoneExileChoice mzec = gameData.interaction.multiZoneExileChoiceContext();
                if (mzec != null) {
                    resendFromContext(gameData, playerId, mzec);
                }
            }
            case COMBAT_DAMAGE_ASSIGNMENT -> {
                InteractionContext.CombatDamageAssignment cda = gameData.interaction.combatDamageAssignmentContext();
                if (cda != null) {
                    resendFromContext(gameData, playerId, cda);
                }
            }
            case X_VALUE_CHOICE -> {
                InteractionContext.XValueChoice xvc = gameData.interaction.xValueChoiceContext();
                if (xvc != null) {
                    resendFromContext(gameData, playerId, xvc);
                }
            }
            case KNOWLEDGE_POOL_CAST_CHOICE -> {
                InteractionContext.KnowledgePoolCastChoice kpc = gameData.interaction.knowledgePoolCastChoiceContext();
                if (kpc != null) {
                    resendFromContext(gameData, playerId, kpc);
                }
            }
        }
    }

    private void resendFromContext(GameData gameData, UUID playerId, InteractionContext context) {
        switch (context) {
            case InteractionContext.AttackerDeclaration ad -> {
                if (playerId.equals(ad.activePlayerId())) {
                    List<Integer> attackable = combatService.getAttackableCreatureIndices(gameData, playerId);
                    List<Integer> mustAttack = combatService.getMustAttackIndices(gameData, playerId, attackable);
                    sessionManager.sendToPlayer(playerId, new AvailableAttackersMessage(attackable, mustAttack));
                }
            }
            case InteractionContext.BlockerDeclaration bd -> {
                UUID defenderId = bd.defenderId();
                if (playerId.equals(defenderId)) {
                    List<Integer> blockable = combatService.getBlockableCreatureIndices(gameData, defenderId);
                    List<Integer> attackerIndices = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
                    List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
                    attackerIndices = attackerIndices.stream()
                            .filter(idx -> !gameQueryService.hasCantBeBlocked(gameData, attackerBattlefield.get(idx)))
                            .toList();
                    var legalPairs = combatService.computeLegalBlockPairs(gameData, blockable, attackerIndices, defenderId, gameData.activePlayerId);
                    sessionManager.sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices, legalPairs));
                }
            }
            case InteractionContext.CardChoice cc -> {
                if (!playerId.equals(cc.playerId())) {
                    return;
                }
                String prompt = switch (cc.type()) {
                    case DISCARD_CHOICE -> "Choose a card to discard.";
                    case ACTIVATED_ABILITY_DISCARD_COST_CHOICE -> gameData.pendingAbilityActivation != null
                            ? "Choose a " + gameData.pendingAbilityActivation.discardCostType().name().toLowerCase()
                            + " card to discard as an activation cost."
                            : "Choose a card from your hand.";
                    default -> "Choose a card from your hand.";
                };
                sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                        new ArrayList<>(cc.validIndices()), prompt));
            }
            case InteractionContext.PermanentChoice pc -> {
                if (playerId.equals(pc.playerId())) {
                    sessionManager.sendToPlayer(playerId, new ChoosePermanentMessage(
                            new ArrayList<>(pc.validIds()), "Choose a permanent."));
                }
            }
            case InteractionContext.GraveyardChoice gc -> {
                if (playerId.equals(gc.playerId())) {
                    sessionManager.sendToPlayer(playerId, new ChooseCardFromGraveyardMessage(
                            new ArrayList<>(gc.validIndices()), "Choose a card from the graveyard.", gc.cardPool() != null));
                }
            }
            case InteractionContext.ColorChoice cc -> {
                if (!playerId.equals(cc.playerId())) {
                    return;
                }
                List<String> options;
                String prompt;
                if (cc.context() instanceof ColorChoiceContext.TextChangeFromWord) {
                    options = new ArrayList<>();
                    options.addAll(GameQueryService.TEXT_CHANGE_COLOR_WORDS);
                    options.addAll(GameQueryService.TEXT_CHANGE_LAND_TYPES);
                    prompt = "Choose a color word or basic land type to replace.";
                } else if (cc.context() instanceof ColorChoiceContext.TextChangeToWord tc) {
                    if (tc.isColor()) {
                        options = GameQueryService.TEXT_CHANGE_COLOR_WORDS.stream().filter(c -> !c.equals(tc.fromWord())).toList();
                        prompt = "Choose the replacement color word.";
                    } else {
                        options = GameQueryService.TEXT_CHANGE_LAND_TYPES.stream().filter(t -> !t.equals(tc.fromWord())).toList();
                        prompt = "Choose the replacement basic land type.";
                    }
                } else if (cc.context() instanceof ColorChoiceContext.DrawReplacementChoice drc
                        && drc.kind() == DrawReplacementKind.ABUNDANCE) {
                    options = List.of("LAND", "NONLAND");
                    prompt = "Choose land or nonland for Abundance.";
                } else if (cc.context() instanceof ColorChoiceContext.CardNameChoice cnc) {
                    if (cnc.excludedTypes().isEmpty()) {
                        options = collectAllCardNamesInGame(gameData);
                        prompt = "Choose a card name.";
                    } else {
                        options = collectCardNamesInGameExcluding(gameData, cnc.excludedTypes());
                        String excludedLabel = cnc.excludedTypes().stream()
                                .map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
                        prompt = "Choose a non" + excludedLabel + " card name.";
                    }
                } else if (cc.context() instanceof ColorChoiceContext.ExileByNameChoice ebn) {
                    options = collectCardNamesInGameExcluding(gameData, ebn.excludedTypes());
                    String excludedLabel = ebn.excludedTypes().stream()
                            .map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
                    prompt = "Choose a non" + excludedLabel + " card name.";
                } else if (cc.context() instanceof ColorChoiceContext.KeywordGrantChoice kgc) {
                    options = kgc.options().stream().map(Keyword::name).toList();
                    prompt = "Choose a keyword to grant.";
                } else if (cc.context() instanceof ColorChoiceContext.SubtypeChoice) {
                    options = java.util.Arrays.stream(CardSubtype.values())
                            .filter(s -> !NON_CREATURE_SUBTYPES.contains(s))
                            .map(CardSubtype::name)
                            .toList();
                    prompt = "Choose a creature type.";
                } else {
                    options = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                    prompt = "Choose a color.";
                }
                sessionManager.sendToPlayer(playerId, new ChooseColorMessage(options, prompt));
            }
            case InteractionContext.MayAbilityChoice mc -> {
                if (playerId.equals(mc.playerId())) {
                    boolean canPay = true;
                    String manaCost = null;
                    if (!gameData.pendingMayAbilities.isEmpty()) {
                        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
                        manaCost = next.manaCost();
                        if (manaCost != null) {
                            ManaCost cost = new ManaCost(manaCost);
                            ManaPool pool = gameData.playerManaPools.get(next.controllerId());
                            canPay = cost.hasX() ? cost.calculateMaxX(pool) > 0 : cost.canPay(pool);
                        }
                    }
                    sessionManager.sendToPlayer(playerId, new MayAbilityMessage(mc.description(), canPay, manaCost));
                }
            }
            case InteractionContext.MultiPermanentChoice mpc -> {
                if (playerId.equals(mpc.playerId())) {
                    sessionManager.sendToPlayer(playerId, new ChooseMultiplePermanentsMessage(
                            new ArrayList<>(mpc.validIds()), mpc.maxCount(), "Choose permanents."));
                }
            }
            case InteractionContext.MultiGraveyardChoice mgc -> {
                if (!playerId.equals(mgc.playerId())) {
                    return;
                }
                List<UUID> validCardIds = new ArrayList<>(mgc.validCardIds());
                List<CardView> cardViews = new ArrayList<>();
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Card> graveyard = gameData.playerGraveyards.get(pid);
                    if (graveyard == null) continue;
                    for (Card card : graveyard) {
                        if (mgc.validCardIds().contains(card.getId())) {
                            cardViews.add(cardViewFactory.create(card));
                        }
                    }
                }
                sessionManager.sendToPlayer(playerId, new ChooseMultipleCardsFromGraveyardsMessage(
                        validCardIds, cardViews, mgc.maxCount(),
                        "Exile up to " + mgc.maxCount() + " cards from graveyards."));
            }
            case InteractionContext.LibraryReorder lr -> {
                if (!playerId.equals(lr.playerId()) || lr.cards() == null) {
                    return;
                }
                List<CardView> cardViews = lr.cards().stream().map(cardViewFactory::create).toList();
                String prompt = lr.toBottom()
                        ? "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
                        : "Put these cards back on top of your library in any order (top to bottom).";
                sessionManager.sendToPlayer(playerId, new ReorderLibraryCardsMessage(cardViews, prompt));
            }
            case InteractionContext.LibrarySearch ls -> {
                if (!playerId.equals(ls.playerId()) || ls.cards() == null) {
                    return;
                }
                List<CardView> cardViews = ls.cards().stream().map(cardViewFactory::create).toList();
                String prompt = ls.prompt() != null && !ls.prompt().isBlank()
                        ? ls.prompt()
                        : ls.canFailToFind()
                            ? "Search your library for a basic land card to put into your hand."
                            : "Search your library for a card to put into your hand.";
                sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(
                        cardViews, prompt, ls.canFailToFind()));
            }
            case InteractionContext.LibraryRevealChoice lrc -> {
                if (!playerId.equals(lrc.playerId()) || lrc.validCardIds() == null) {
                    return;
                }
                List<CardView> cardViews = lrc.allCards().stream()
                        .filter(c -> lrc.validCardIds().contains(c.getId()))
                        .map(cardViewFactory::create)
                        .toList();
                List<UUID> cardIds = new ArrayList<>(lrc.validCardIds());
                sessionManager.sendToPlayer(playerId, new ChooseMultipleCardsFromGraveyardsMessage(
                        cardIds, cardViews, cardIds.size(),
                        "Choose any number of nonland permanent cards with mana value 3 or less to put onto the battlefield."
                ));
            }
            case InteractionContext.HandTopBottomChoice htbc -> {
                if (!playerId.equals(htbc.playerId()) || htbc.cards() == null) {
                    return;
                }
                List<CardView> cardViews = htbc.cards().stream().map(cardViewFactory::create).toList();
                int count = htbc.cards().size();
                sessionManager.sendToPlayer(playerId, new ChooseHandTopBottomMessage(
                        cardViews, "Look at the top " + count + " cards of your library. Choose one to put into your hand."));
            }
            case InteractionContext.RevealedHandChoice rhc -> {
                if (!playerId.equals(rhc.choosingPlayerId()) || rhc.targetPlayerId() == null) {
                    return;
                }
                UUID targetPlayerId = rhc.targetPlayerId();
                List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
                String targetName = gameData.playerIdToName.get(targetPlayerId);
                List<CardView> cardViews = targetHand.stream().map(cardViewFactory::create).toList();
                List<Integer> validIndices = new ArrayList<>(rhc.validIndices());
                sessionManager.sendToPlayer(playerId, new ChooseFromRevealedHandMessage(
                        cardViews, validIndices, "Choose a card to put on top of " + targetName + "'s library."));
            }
            case InteractionContext.MultiZoneExileChoice mzec -> {
                if (!playerId.equals(mzec.playerId())) {
                    return;
                }
                List<UUID> validCardIds = new ArrayList<>(mzec.validCardIds());
                List<CardView> cardViews = new ArrayList<>();
                // Collect CardViews from hand, graveyard, and library of target player
                UUID targetPid = mzec.targetPlayerId();
                for (Card card : gameData.playerHands.getOrDefault(targetPid, List.of())) {
                    if (mzec.validCardIds().contains(card.getId())) {
                        cardViews.add(cardViewFactory.create(card));
                    }
                }
                for (Card card : gameData.playerGraveyards.getOrDefault(targetPid, List.of())) {
                    if (mzec.validCardIds().contains(card.getId())) {
                        cardViews.add(cardViewFactory.create(card));
                    }
                }
                for (Card card : gameData.playerDecks.getOrDefault(targetPid, List.of())) {
                    if (mzec.validCardIds().contains(card.getId())) {
                        cardViews.add(cardViewFactory.create(card));
                    }
                }
                sessionManager.sendToPlayer(playerId, new ChooseMultipleCardsFromGraveyardsMessage(
                        validCardIds, cardViews, mzec.maxCount(),
                        "Choose any number of cards named \"" + mzec.cardName() + "\" to exile."));
            }
            case InteractionContext.CombatDamageAssignment cda -> {
                if (!playerId.equals(cda.playerId())) {
                    return;
                }
                List<CombatDamageTargetView> targetViews = cda.validTargets().stream()
                        .map(t -> new CombatDamageTargetView(
                                t.id().toString(), t.name(), t.effectiveToughness(), t.currentDamage(), t.isPlayer()))
                        .toList();
                sessionManager.sendToPlayer(playerId, new CombatDamageAssignmentNotification(
                        cda.attackerIndex(), cda.attackerPermanentId().toString(),
                        cda.attackerName(), cda.totalDamage(), targetViews, cda.isTrample(), cda.isDeathtouch()));
            }
            case InteractionContext.XValueChoice xvc -> {
                if (playerId.equals(xvc.playerId())) {
                    sessionManager.sendToPlayer(playerId, new XValueChoiceMessage(
                            xvc.prompt(), xvc.maxValue(), xvc.cardName()));
                }
            }
            case InteractionContext.KnowledgePoolCastChoice kpc -> {
                if (!playerId.equals(kpc.playerId())) {
                    return;
                }
                List<UUID> validCardIds = new ArrayList<>(kpc.validCardIds());
                List<CardView> cardViews = new ArrayList<>();
                // Collect CardViews from the KP pool
                UUID kpPermanentId = gameData.knowledgePoolSourcePermanentId;
                if (kpPermanentId != null) {
                    List<Card> pool = gameData.permanentExiledCards.get(kpPermanentId);
                    if (pool != null) {
                        for (Card card : pool) {
                            if (kpc.validCardIds().contains(card.getId())) {
                                cardViews.add(cardViewFactory.create(card));
                            }
                        }
                    }
                }
                sessionManager.sendToPlayer(playerId, new ChooseMultipleCardsFromGraveyardsMessage(
                        validCardIds, cardViews, 1,
                        "Knowledge Pool — you may cast a nonland card without paying its mana cost."));
            }
            case InteractionContext.Scry s -> {
                if (!playerId.equals(s.playerId()) || s.cards() == null) {
                    return;
                }
                List<CardView> cardViews = s.cards().stream().map(cardViewFactory::create).toList();
                String prompt = s.cards().size() == 1
                        ? "Scry 1: Keep on top or put on the bottom of your library."
                        : "Scry " + s.cards().size() + ": Put cards on the top or bottom of your library.";
                sessionManager.sendToPlayer(playerId, new ScryMessage(cardViews, prompt));
            }
        }
    }

    private List<String> collectAllCardNamesInGame(GameData gameData) {
        java.util.Set<String> names = new java.util.TreeSet<>();
        for (java.util.UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of())
                    .forEach(p -> names.add(p.getCard().getName()));
            gameData.playerHands.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerGraveyards.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerDecks.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerExiledCards.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
        }
        gameData.stack.forEach(se -> names.add(se.getCard().getName()));
        return new java.util.ArrayList<>(names);
    }

    private List<String> collectCardNamesInGameExcluding(GameData gameData, java.util.List<com.github.laxika.magicalvibes.model.CardType> excludedTypes) {
        java.util.Set<String> names = new java.util.TreeSet<>();
        for (java.util.UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of()).stream()
                    .filter(p -> !hasExcludedType(p.getCard(), excludedTypes))
                    .forEach(p -> names.add(p.getCard().getName()));
            gameData.playerHands.getOrDefault(pid, List.of()).stream()
                    .filter(c -> !hasExcludedType(c, excludedTypes))
                    .forEach(c -> names.add(c.getName()));
            gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                    .filter(c -> !hasExcludedType(c, excludedTypes))
                    .forEach(c -> names.add(c.getName()));
            gameData.playerDecks.getOrDefault(pid, List.of()).stream()
                    .filter(c -> !hasExcludedType(c, excludedTypes))
                    .forEach(c -> names.add(c.getName()));
            gameData.playerExiledCards.getOrDefault(pid, List.of()).stream()
                    .filter(c -> !hasExcludedType(c, excludedTypes))
                    .forEach(c -> names.add(c.getName()));
        }
        gameData.stack.stream()
                .filter(se -> !hasExcludedType(se.getCard(), excludedTypes))
                .forEach(se -> names.add(se.getCard().getName()));
        return new java.util.ArrayList<>(names);
    }

    private boolean hasExcludedType(com.github.laxika.magicalvibes.model.Card card, java.util.List<com.github.laxika.magicalvibes.model.CardType> excludedTypes) {
        if (excludedTypes.contains(card.getType())) {
            return true;
        }
        for (com.github.laxika.magicalvibes.model.CardType excluded : excludedTypes) {
            if (card.getAdditionalTypes().contains(excluded)) {
                return true;
            }
        }
        return false;
    }
}
