package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.*;
import java.util.*;

/** A selection view over external cards and cards owned in suspended ancestor games. */
public final class OutsideGameCards {
    private OutsideGameCards() { }

    public static List<Card> view(GameData game, UUID owner) {
        if (game.session.isRoot(game)) return game.playerSideboards.getOrDefault(owner, List.of());
        Map<UUID, Card> cards = new LinkedHashMap<>();
        Map<UUID, UUID> origins = new HashMap<>();
        game.playerSideboards.getOrDefault(owner, List.of()).forEach(card -> cards.put(card.getId(), card));
        Set<UUID> pending = new HashSet<>();
        game.pendingAncestorTransfers.forEach(transfer -> pending.add(transfer.cardId()));
        for (GameData ancestor : game.session.frames()) {
            if (ancestor == game) break;
            java.util.function.Consumer<Card> add = card -> {
                if (card != null && !card.isToken() && !pending.contains(card.getId())
                        && (card.getOwnerId() == null || owner.equals(card.getOwnerId()))) {
                    cards.putIfAbsent(card.getId(), card);
                    origins.put(card.getId(), ancestor.id);
                }
            };
            ancestor.playerHands.getOrDefault(owner, List.of()).forEach(add);
            ancestor.playerDecks.getOrDefault(owner, List.of()).forEach(add);
            ancestor.playerGraveyards.getOrDefault(owner, List.of()).forEach(add);
            ancestor.getPlayerExiledCards(owner).forEach(add);
            java.util.stream.Stream.concat(ancestor.playerBattlefields.entrySet().stream(),
                    ancestor.phasedOutPermanents.entrySet().stream()).forEach(zone -> zone.getValue().forEach(permanent -> {
                UUID controller = zone.getKey();
                UUID physicalOwner = permanent.getOriginalCard().getOwnerId();
                if (physicalOwner == null) physicalOwner = ancestor.stolenCreatures.getOrDefault(permanent.getId(), controller);
                if (owner.equals(physicalOwner)) permanent.cardsLeavingBattlefield().forEach(add);
            }));
            ancestor.stack.forEach(entry -> addSpell(entry, owner, add));
            if (ancestor.pendingEffectResolutionEntry != null) addSpell(ancestor.pendingEffectResolutionEntry, owner, add);
        }
        List<Card> snapshot = new ArrayList<>(cards.values());
        return new AbstractList<>() {
            public Card get(int index) { return snapshot.get(index); }
            public int size() { return snapshot.size(); }
            public Card remove(int index) {
                Card card = snapshot.remove(index);
                game.subgameCards.put(card.getId(), card);
                UUID source = origins.get(card.getId());
                if (source == null) game.playerSideboards.get(owner).remove(card);
                else game.pendingAncestorTransfers.add(new PendingAncestorTransfer(source, card.getId()));
                return card;
            }
        };
    }

    private static void addSpell(StackEntry entry, UUID owner, java.util.function.Consumer<Card> add) {
        if (owner.equals(entry.getOwnerId()) && !entry.isCopy() && !entry.isSpellDispositionHandled()
                && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY) add.accept(entry.getPhysicalCard());
    }
}
