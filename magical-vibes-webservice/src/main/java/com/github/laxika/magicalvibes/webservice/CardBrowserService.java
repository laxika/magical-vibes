package com.github.laxika.magicalvibes.webservice;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.carddata.FaceOracleMapper;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.carddata.store.PrintVariant;
import com.github.laxika.magicalvibes.carddata.store.SetDataStore;
import com.github.laxika.magicalvibes.carddata.store.StoredPrinting;
import com.github.laxika.magicalvibes.carddata.store.StoredSet;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.networking.message.BrowseCardInfo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Every base printing of a set, implemented or not, for the card browser and the deck builder.
 *
 * <p>Built from the stored set data, so it shows the same cards whichever provider a set was
 * downloaded from, and its faces go through {@link FaceOracleMapper} exactly as the engine's do.
 */
@Service
public class CardBrowserService {

    private static final Logger LOG = Logger.getLogger(CardBrowserService.class.getName());

    /** Digits, optionally followed by an art-variant letter used by older expansions. */
    private static final Pattern BASE_COLLECTOR_NUMBER = Pattern.compile("\\d+[a-d]?");

    /**
     * Promo types that are still playable set printings (numbered above the base set) and
     * should appear in the deck editor / card browser. Everything else with promo types
     * stays filtered out (buy-a-box, prerelease, etc.). Allowed printings may still carry showcase
     * frames (ALA #250); those frame filters are skipped when an allowed promo type is present.
     */
    private static final Set<String> ALLOWED_PROMO_TYPES = Set.of("planeswalkerdeck", "setextension");

    private final ConcurrentHashMap<String, List<BrowseCardInfo>> cache = new ConcurrentHashMap<>();

    private final CardCatalog cardCatalog;
    private final SetDataStore setDataStore;

    public CardBrowserService(CardCatalog cardCatalog, SetDataStore setDataStore) {
        this.cardCatalog = cardCatalog;
        this.setDataStore = setDataStore;
    }

    /**
     * @return an empty list when the set cannot be loaded; that result is not remembered, so the
     * next request for the set tries again
     */
    public List<BrowseCardInfo> getCardsForSet(String setCode) {
        List<BrowseCardInfo> cached = cache.get(setCode);
        if (cached != null) {
            return cached;
        }
        try {
            List<BrowseCardInfo> cards = buildCards(setCode, setDataStore.get(setCode));
            cache.put(setCode, cards);
            return cards;
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.WARNING, "Could not load the card list for set " + setCode, e);
            return List.of();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    List<BrowseCardInfo> buildCards(String setCode, StoredSet set) {
        CardSet cardSet = findCardSet(setCode);
        Set<String> implementedNumbers = cardSet == null ? Set.of()
                : cardCatalog.getPrintings(cardSet).stream()
                .map(CardPrinting::collectorNumber)
                .collect(Collectors.toSet());

        List<BrowseCardInfo> cards = new ArrayList<>();
        for (StoredPrinting printing : set.printings()) {
            String collectorNumber = printing.collectorNumber();

            // Keep base printings and multi-art lettered variants (Homelands/Alliances
            // 10a/10b, Fallen Empires a–d). Skip foil/promo/star suffixes (139s, 278★, …).
            if (!BASE_COLLECTOR_NUMBER.matcher(collectorNumber).matches()) {
                continue;
            }

            // Skip showcase, extended art, borderless, promo, and full-art variants
            if (isSpecialPrint(printing.variant())) {
                continue;
            }

            String rarity = CardPrintingRegistry.getRarity(setCode, collectorNumber);
            if (rarity == null) {
                rarity = printing.rarity() != null ? printing.rarity() : "common";
            }

            // Both faces of a double-faced card are printed with the same mark.
            String watermark = printing.front().watermark();
            if (watermark != null && watermark.isEmpty()) {
                watermark = null;
            }

            boolean implemented = implementedNumbers.contains(collectorNumber);
            List<String> promoTypes = printing.variant().promoTypes();

            BrowseCardInfo secondFace = printing.back() == null ? null
                    : buildFaceInfo(printing.back(), true, collectorNumber, setCode, rarity,
                    watermark, implemented, null, null, promoTypes);
            // A prepare card's second face is the spell printed inset on its front, not a
            // face you turn the card over to see. Handing it back as a back face is what
            // made prepare cards render with a flip control, like a transform DFC.
            boolean prepare = "prepare".equals(printing.front().layout());
            cards.add(buildFaceInfo(printing.front(), false, collectorNumber, setCode, rarity,
                    watermark, implemented,
                    prepare ? null : secondFace, prepare ? secondFace : null, promoTypes));
        }
        return cards;
    }

    /**
     * Card info for one face. The browser shows a card's halves one at a time, so a name, cost or
     * type line spanning both ("Flotsam // Jetsam") is cut to this face's own before mapping.
     */
    private static BrowseCardInfo buildFaceInfo(RawFace face, boolean isBackFace,
                                                String collectorNumber, String setCode,
                                                String rarity, String watermark, boolean implemented,
                                                BrowseCardInfo backFace, BrowseCardInfo prepareSpell,
                                                List<String> promoTypes) {
        String typeLine = firstFace(face.typeLine());
        OracleData data = FaceOracleMapper.toOracleData(new RawFace(
                firstFace(face.name()), firstFace(face.manaCost()), typeLine, face.text(),
                face.colors(), face.colorIndicator(), face.colorIdentity(), face.power(),
                face.toughness(), face.loyalty(), face.defense(), face.keywords(), face.watermark(),
                face.layout(), null), isBackFace);

        return new BrowseCardInfo(
                data.name(), collectorNumber, setCode, data.manaCost(), typeLine == null ? "" : typeLine,
                rarity, face.power(), face.toughness(), displayColor(data), names(data.colors()),
                implemented, data.cardText(), names(data.keywords()),
                data.type() != null ? data.type().name() : CardType.CREATURE.name(),
                names(data.additionalTypes()), names(data.supertypes()), names(data.subtypes()),
                data.loyalty(), watermark, backFace, prepareSpell, promoTypes);
    }

    /**
     * The single colour to tint the card with: its colour if it has exactly one, or for a
     * colourless land the first colour of its identity, as the land frames are printed.
     */
    private static String displayColor(OracleData data) {
        if (data.colors().size() == 1) {
            return data.colors().getFirst().name();
        }
        boolean land = data.type() == CardType.LAND || data.additionalTypes().contains(CardType.LAND);
        if (data.colors().isEmpty() && land && !data.colorIdentity().isEmpty()) {
            return data.colorIdentity().getFirst().name();
        }
        return null;
    }

    private static String firstFace(String value) {
        return value != null && value.contains(" // ") ? value.substring(0, value.indexOf(" // ")) : value;
    }

    private static List<String> names(Collection<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).toList();
    }

    private static boolean isSpecialPrint(PrintVariant variant) {
        // Planeswalker-deck / set-extension printings are part of the set's numbered extras;
        // keep them even when they also carry a showcase frame or boosterfun promo type.
        if (variant.promoTypes().stream().anyMatch(ALLOWED_PROMO_TYPES::contains)) {
            return false;
        }
        if (variant.frameEffects().contains("showcase") || variant.frameEffects().contains("extendedart")) {
            return true;
        }
        if ("borderless".equals(variant.borderColor())) {
            return true;
        }
        return !variant.promoTypes().isEmpty() || variant.fullArt();
    }

    private CardSet findCardSet(String setCode) {
        for (CardSet cs : CardSet.values()) {
            if (cs.getCode().equalsIgnoreCase(setCode)) {
                return cs;
            }
        }
        return null;
    }
}
