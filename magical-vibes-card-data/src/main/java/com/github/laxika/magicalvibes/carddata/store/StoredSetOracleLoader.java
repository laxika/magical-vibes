package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry.TokenImageData;
import com.github.laxika.magicalvibes.carddata.FaceOracleMapper;
import com.github.laxika.magicalvibes.carddata.LegalitySnapshot;
import com.github.laxika.magicalvibes.carddata.OracleLoader;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.carddata.SetOracleData;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The {@link OracleLoader}: reads a set from the {@link SetDataStore} and maps the faces of its
 * implemented printings through {@link FaceOracleMapper}.
 *
 * <p>Provider-neutral by construction — a {@link StoredSet} looks the same whichever provider it
 * was converted from, so nothing here knows or asks where the data came from.
 */
@Service
public class StoredSetOracleLoader implements OracleLoader {

    private static final Logger LOG = Logger.getLogger(StoredSetOracleLoader.class.getName());

    private final SetDataStore store;

    public StoredSetOracleLoader(SetDataStore store) {
        this.store = store;
    }

    /**
     * Stored with the set and never refreshed on its own, like the rest of it. {@code updatedAt} is
     * when the set was downloaded.
     */
    @Override
    public LegalitySnapshot loadLegalities(String setCode) {
        try {
            StoredSet set = store.get(setCode);
            Map<String, Map<String, String>> legalities = new HashMap<>();
            for (StoredPrinting printing : set.printings()) {
                legalities.put(printing.collectorNumber(), printing.legalities());
            }
            return new LegalitySnapshot(legalities, set.fetchedAt());
        } catch (IOException e) {
            LOG.warning("Could not load legalities for " + setCode + ": " + e.getMessage());
            return LegalitySnapshot.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return LegalitySnapshot.empty();
        }
    }

    @Override
    public SetOracleData loadSet(String setCode, Set<String> implementedCollectorNumbers) {
        try {
            return toSetOracleData(setCode, store.get(setCode), implementedCollectorNumbers);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load oracle data for set " + setCode, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while loading oracle data for set " + setCode, e);
        }
    }

    static SetOracleData toSetOracleData(String setCode, StoredSet set,
                                         Set<String> implementedCollectorNumbers) {
        Map<String, StoredPrinting> printings = new LinkedHashMap<>();
        Map<String, String> rarities = new HashMap<>();
        for (StoredPrinting printing : set.printings()) {
            printings.put(printing.collectorNumber(), printing);
            if (printing.rarity() != null) {
                rarities.put(printing.collectorNumber(), printing.rarity());
            }
        }
        mergeImplementedTokenPrintings(printings, set.tokens(), implementedCollectorNumbers);
        int cardTotal = printings.size();
        CardDataSupport.applyMissingPrintingAliases(setCode, printings, rarities);

        // Faces are mapped only for printings the game implements.
        Map<String, OracleData> frontFaces = new HashMap<>();
        Map<String, OracleData> backFaces = new HashMap<>();
        Map<String, List<String>> faceNames = new HashMap<>();
        for (String collectorNumber : implementedCollectorNumbers) {
            StoredPrinting printing = printings.get(collectorNumber);
            if (printing == null) {
                continue;
            }
            frontFaces.put(collectorNumber, frontFace(printing));
            if (!printing.faceNames().isEmpty()) {
                faceNames.put(collectorNumber, printing.faceNames());
            }
            if (printing.back() != null) {
                backFaces.put(collectorNumber, backFace(printing));
            }
        }

        // Neither provider lists MB2's playable emblem printing, while the official printing
        // exposes it as collector number 513.
        if ("MB2".equalsIgnoreCase(setCode) && implementedCollectorNumbers.contains("513")
                && !frontFaces.containsKey("513")) {
            frontFaces.put("513", new OracleData(
                    "Essence of Ajani", CardType.EMBLEM, Set.of(), "{2}{W}", CardColor.WHITE,
                    List.of(CardColor.WHITE), List.of(CardColor.WHITE), Set.of(), List.of(),
                    "(As this spell resolves, put it into the command zone.)\n"
                            + "Whenever you cast a spell, you gain 1 life.",
                    null, null, Set.of(), null, null, null));
            rarities.putIfAbsent("513", "rare");
        }

        return new SetOracleData(set.setName(), cardTotal, rarities,
                frontFaces, backFaces, faceNames, tokenImages(setCode, set));
    }

    /** The printing's front face, or for a split card the whole card with its halves attached. */
    public static OracleData frontFace(StoredPrinting printing) {
        return printing.splitHalves().isEmpty()
                ? FaceOracleMapper.toOracleData(printing.front(), false)
                : FaceOracleMapper.toSplitCardOracleData(printing.front(), printing.splitHalves());
    }

    /** The printing's back face, or null when it has none. */
    public static OracleData backFace(StoredPrinting printing) {
        return printing.back() == null ? null : FaceOracleMapper.toOracleData(printing.back(), true);
    }

    /**
     * A token a set registers a class for at the token's own collector number becomes one of the
     * set's printings, so the class gets the token's oracle data. A real printing at that number
     * always wins.
     */
    private static void mergeImplementedTokenPrintings(Map<String, StoredPrinting> printings,
                                                       List<StoredToken> tokens,
                                                       Set<String> implementedCollectorNumbers) {
        for (StoredToken token : tokens) {
            if (implementedCollectorNumbers.contains(token.collectorNumber())) {
                printings.putIfAbsent(token.collectorNumber(), new StoredPrinting(
                        token.collectorNumber(), null, List.of(), token.face(), null, List.of(),
                        null, Map.of()));
            }
        }
    }

    /**
     * Token art keyed by {@link CardPrintingRegistry#buildTokenKey}, for creature and non-creature
     * tokens (Treasure, Food, …) alike. Emblems are skipped, and so is every face of a double-faced
     * token but its front.
     */
    private static Map<String, TokenImageData> tokenImages(String setCode, StoredSet set) {
        Map<String, TokenImageData> tokenMap = new HashMap<>();
        for (StoredToken token : set.tokens()) {
            RawFace face = token.face();
            if (token.side() != null && !"a".equals(token.side())) {
                continue;
            }
            String typeLine = face.typeLine() == null ? "" : face.typeLine();
            if (typeLine.contains("Emblem")) {
                continue;
            }

            boolean isCreature = typeLine.contains("Creature");
            Integer power = isCreature ? CardDataSupport.parseInt(face.power()) : null;
            Integer toughness = isCreature ? CardDataSupport.parseInt(face.toughness()) : null;
            List<CardColor> colors = FaceOracleMapper.mapColors(face.colors());
            CardColor color = colors.isEmpty() ? null : colors.get(0);

            String key = CardPrintingRegistry.buildTokenKey(face.name(), power, toughness, color);
            tokenMap.put(key, new TokenImageData(set.tokenSetCode(), token.collectorNumber()));
        }
        if (!tokenMap.isEmpty()) {
            LOG.info("Loaded " + tokenMap.size() + " token images for set " + setCode);
        }
        return tokenMap;
    }
}
