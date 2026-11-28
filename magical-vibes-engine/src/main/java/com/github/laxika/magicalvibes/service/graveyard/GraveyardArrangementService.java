package com.github.laxika.magicalvibes.service.graveyard;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SimultaneousGraveyardArrival;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CatalogCardGenerator;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.GraveyardOrderReader;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Lets a player arrange the cards put into their graveyard at the same time (CR 404.3), only where the order can
 * matter.
 *
 * <p>The order of a graveyard matters only to a card that reads it ({@link GraveyardOrderReader}: "the top card of
 * your graveyard", "the top creature card", "the bottom card"), so the engine asks only in a game where such a card
 * can appear: one in any zone of any player, in a sideboard (a wish can fetch it), or among the cards any of those
 * can make (the printings an effect names, transitively; an effect that picks from every card,
 * {@link CatalogCardGenerator}, can make any reader). Every player's graveyard counts, since a reader can name
 * another player's graveyard or change control. When the readers only read the order of one type's cards ("the top
 * creature card"), only cards of those types are offered; the arrangement is skipped when fewer than two such cards
 * arrived, or when they are interchangeable copies.
 *
 * <p>The cards a rules event batch puts into one graveyard together are recorded as the batch closes
 * ({@link GameData#simultaneousGraveyardArrivals}). The engine puts them there in its own order and offers the
 * arrangement before a player next receives priority, before the triggered abilities are ordered: nothing reads a
 * graveyard's order between, so arranging the cards in place is the same as putting them in that order. A card that
 * left the graveyard meanwhile is not offered.
 */
@Service
@RequiredArgsConstructor
public class GraveyardArrangementService {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;

    /** What reading a card class's text does to graveyard order, and the cards it can make, by card class. */
    private final Map<Class<? extends Card>, CardScan> scansByClass = new ConcurrentHashMap<>();
    /** The same for the card of a printing an effect names, by printing. */
    private final Map<SpellbookCard, Optional<CardScan>> scansByPrinting = new ConcurrentHashMap<>();

    /**
     * Offers the oldest recorded arrival whose order can matter to its owner, dropping the ones whose order can't.
     *
     * @return true when an arrangement prompt was opened
     */
    public boolean beginNextArrangement(GameData gameData) {
        if (gameData.simultaneousGraveyardArrivals.isEmpty()) {
            return false;
        }
        OrderRead read = null;
        while (!gameData.simultaneousGraveyardArrivals.isEmpty()) {
            SimultaneousGraveyardArrival arrival = gameData.simultaneousGraveyardArrivals.removeFirst();
            if (read == null) {
                read = orderRead(gameData);
            }
            if (read.isNone()) {
                gameData.simultaneousGraveyardArrivals.clear();
                return false;
            }
            List<Card> cards = arrangeableCards(gameData, arrival, read);
            if (cards.size() < 2 || allInterchangeable(gameData, cards)) {
                continue;
            }
            List<UUID> ids = cards.stream().map(Card::getId).toList();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    arrival.ownerId(), null, null,
                    new ChoiceContext.GraveyardArrangement(arrival.ownerId(), ids, List.of()),
                    labels(cards),
                    "Choose the card to put into your graveyard first (it goes below the others)."));
            return true;
        }
        return false;
    }

    /**
     * Records {@code index} (of the remaining cards' labels) as the next card put into the graveyard and asks for the
     * next one, or, with one card left, puts the cards in the chosen order into the positions they hold.
     *
     * @return true when the arrangement is complete, false when the next card is asked for
     */
    public boolean answer(GameData gameData, UUID playerId, ChoiceContext.GraveyardArrangement arrangement, int index) {
        if (!playerId.equals(arrangement.ownerId())) {
            throw new IllegalStateException("Not your graveyard to arrange");
        }
        if (index < 0 || index >= arrangement.remainingIds().size()) {
            throw new IllegalArgumentException("Invalid graveyard arrangement choice");
        }
        List<UUID> remaining = new ArrayList<>(arrangement.remainingIds());
        List<UUID> ordered = new ArrayList<>(arrangement.orderedIds());
        ordered.add(remaining.remove(index));
        gameData.interaction.clearAwaitingInput();
        List<Card> graveyard = gameData.playerGraveyards.get(arrangement.ownerId());
        if (remaining.size() > 1) {
            List<Card> remainingCards = remaining.stream().map(id -> findCard(graveyard, id)).toList();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    playerId, null, null,
                    new ChoiceContext.GraveyardArrangement(arrangement.ownerId(), remaining, ordered),
                    labels(remainingCards),
                    "Choose the next card to put into your graveyard."));
            return false;
        }
        ordered.addAll(remaining);
        arrange(graveyard, ordered);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " arranges "
                + ordered.size() + " cards put into their graveyard at the same time."));
        return true;
    }

    /** Puts the cards {@code orderedIds} names, bottom first, into the graveyard positions they hold among them. */
    private static void arrange(List<Card> graveyard, List<UUID> orderedIds) {
        synchronized (graveyard) {
            List<Integer> positions = new ArrayList<>();
            List<Card> cards = new ArrayList<>();
            for (UUID id : orderedIds) {
                for (int i = 0; i < graveyard.size(); i++) {
                    if (graveyard.get(i).getId().equals(id)) {
                        positions.add(i);
                        cards.add(graveyard.get(i));
                        break;
                    }
                }
            }
            List<Integer> sorted = new ArrayList<>(positions);
            Collections.sort(sorted);
            for (int i = 0; i < cards.size(); i++) {
                graveyard.set(sorted.get(i), cards.get(i));
            }
        }
    }

    private static Card findCard(List<Card> graveyard, UUID id) {
        synchronized (graveyard) {
            return graveyard.stream().filter(card -> card.getId().equals(id)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Card to arrange left the graveyard"));
        }
    }

    private static List<String> labels(List<Card> cards) {
        return IntStream.range(0, cards.size()).mapToObj(i -> (i + 1) + ": " + cards.get(i).getName()).toList();
    }

    /** The arrival's cards still in their owner's graveyard, bottom first, that the readers can tell apart by order. */
    private static List<Card> arrangeableCards(GameData gameData, SimultaneousGraveyardArrival arrival, OrderRead read) {
        List<Card> graveyard = gameData.playerGraveyards.get(arrival.ownerId());
        if (graveyard == null) {
            return List.of();
        }
        Set<UUID> ids = new HashSet<>(arrival.cardIds());
        synchronized (graveyard) {
            return graveyard.stream()
                    .filter(card -> ids.contains(card.getId()))
                    .filter(card -> read.whole() || read.types().stream().anyMatch(card::hasType))
                    .toList();
        }
    }

    /**
     * Whether every card is a copy of the first that no order-reader can tell apart from it: the same card, with
     * nothing made to it since (conjured, or perpetually changed). Anything else counts as different.
     */
    private static boolean allInterchangeable(GameData gameData, List<Card> cards) {
        Card first = cards.getFirst();
        return cards.stream().allMatch(card -> card.getClass() == first.getClass()
                && card.getClass() != Card.class
                && java.util.Objects.equals(card.getName(), first.getName())
                && unchanged(gameData, card));
    }

    private static boolean unchanged(GameData gameData, Card card) {
        return !card.isConjured()
                && !gameData.perpetualCardPowerToughnessModifiers.containsKey(card.getId())
                && !gameData.perpetualCardBasePowerToughness.containsKey(card.getId())
                && gameData.perpetualAbilities(card.getId()).isEmpty()
                && gameData.perpetualStaticEffects(card.getId()).isEmpty();
    }

    /** The graveyard order the cards that can appear in this game read. */
    OrderRead orderRead(GameData gameData) {
        Set<SpellbookCard> seenPrintings = new HashSet<>();
        Deque<SpellbookCard> printings = new ArrayDeque<>();
        boolean[] whole = {false};
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        Consumer<CardScan> add = scan -> {
            whole[0] |= scan.whole() || scan.anyCard();
            types.addAll(scan.types());
            scan.printings().stream().filter(seenPrintings::add).forEach(printings::add);
        };
        Set<Class<?>> seenClasses = new HashSet<>();
        forEachCardInGame(gameData, card -> {
            if (card.getClass() == Card.class || seenClasses.add(card.getClass())) {
                add.accept(scan(card));
            }
        });
        while (!printings.isEmpty() && !whole[0]) {
            scanOfPrinting(printings.removeFirst()).ifPresent(add);
        }
        return new OrderRead(whole[0], whole[0] ? Set.of() : Set.copyOf(types));
    }

    /** Every card in the game, in every zone of every player and in the sideboards. */
    private static void forEachCardInGame(GameData gameData, Consumer<Card> action) {
        Consumer<Card> nonNull = card -> {
            if (card != null) {
                action.accept(card);
            }
        };
        List.of(gameData.playerHands, gameData.playerDecks, gameData.playerGraveyards, gameData.playerSideboards,
                gameData.playerCommandZones).forEach(zones -> zones.values().forEach(cards -> {
                    synchronized (cards) {
                        new ArrayList<>(cards).forEach(nonNull);
                    }
                }));
        synchronized (gameData.exiledCards) {
            gameData.exiledCards.forEach(entry -> nonNull.accept(entry.card()));
        }
        List.of(gameData.playerBattlefields, gameData.phasedOutPermanents).forEach(zones ->
                zones.values().forEach(permanents -> {
                    synchronized (permanents) {
                        for (Permanent permanent : new ArrayList<>(permanents)) {
                            nonNull.accept(permanent.getCard());
                            nonNull.accept(permanent.getOriginalCard());
                        }
                    }
                }));
        synchronized (gameData.stack) {
            gameData.stack.stream().map(StackEntry::getCard).forEach(nonNull);
        }
        if (gameData.pendingEffectResolutionEntry != null) {
            nonNull.accept(gameData.pendingEffectResolutionEntry.getCard());
        }
    }

    private CardScan scan(Card card) {
        if (card.getClass() == Card.class) {
            return CardScan.of(card);
        }
        return scansByClass.computeIfAbsent(card.getClass(), type -> CardScan.of(card));
    }

    private Optional<CardScan> scanOfPrinting(SpellbookCard printing) {
        return scansByPrinting.computeIfAbsent(printing, key -> Optional
                .ofNullable(cardCatalog.createCardIfImplemented(key.setCode(), key.collectorNumber()))
                .map(this::scan));
    }

    /** The graveyard order the cards that can appear read: all of it, or only the order among cards of {@code types}. */
    record OrderRead(boolean whole, Set<CardType> types) {

        boolean isNone() {
            return !whole && types.isEmpty();
        }
    }

    /**
     * What a card's text reads of graveyard order and what it can make: walked from the card through every record,
     * collection and engine object its abilities hold.
     */
    private record CardScan(boolean whole, Set<CardType> types, Set<SpellbookCard> printings, boolean anyCard) {

        static CardScan of(Card card) {
            Walker walker = new Walker();
            walker.visit(card);
            return new CardScan(walker.whole, Set.copyOf(walker.types), Set.copyOf(walker.printings), walker.anyCard);
        }
    }

    private static final class Walker {

        private static final String ENGINE_PACKAGE = "com.github.laxika.magicalvibes.";

        private final Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        private boolean whole;
        private final EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        private final Set<SpellbookCard> printings = new HashSet<>();
        private boolean anyCard;

        void visit(Object value) {
            if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean
                    || value instanceof Character || value instanceof UUID || value instanceof Enum<?>
                    || value instanceof Class<?> || !seen.add(value)) {
                return;
            }
            if (value instanceof GraveyardOrderReader reader) {
                CardType type = reader.graveyardOrderType();
                if (type == null) {
                    whole = true;
                } else {
                    types.add(type);
                }
            }
            if (value instanceof CatalogCardGenerator) {
                anyCard = true;
            }
            if (value instanceof SpellbookCard printing) {
                printings.add(printing);
                return;
            }
            if (value instanceof Iterable<?> iterable) {
                List<Object> elements = new ArrayList<>();
                synchronized (iterable) {
                    iterable.forEach(elements::add);
                }
                elements.forEach(this::visit);
                return;
            }
            if (value instanceof Map<?, ?> map) {
                List<Object> entries = new ArrayList<>();
                synchronized (map) {
                    map.forEach((key, element) -> {
                        entries.add(key);
                        entries.add(element);
                    });
                }
                entries.forEach(this::visit);
                return;
            }
            if (value instanceof Optional<?> optional) {
                optional.ifPresent(this::visit);
                return;
            }
            Class<?> type = value.getClass();
            if (type.isArray()) {
                if (!type.getComponentType().isPrimitive()) {
                    for (Object element : (Object[]) value) {
                        visit(element);
                    }
                }
                return;
            }
            if (type.isSynthetic() || type.isHidden() || !type.getName().startsWith(ENGINE_PACKAGE)) {
                return;
            }
            for (Class<?> current = type; current != null && current != Object.class;
                 current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()
                            || field.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        field.setAccessible(true);
                        visit(field.get(value));
                    } catch (IllegalAccessException | RuntimeException e) {
                        // A field the walk can't read holds no card text.
                    }
                }
            }
        }
    }
}
