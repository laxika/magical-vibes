package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry.TokenImageData;
import com.github.laxika.magicalvibes.model.OracleData;

import java.util.Map;

/**
 * Everything one {@link OracleLoader} read about a single set, in provider-neutral form.
 *
 * <p>A return value rather than a set of writes: the loader parses, {@link CardRegistry} decides
 * what to do with the result. That keeps the provider-specific code free of any knowledge of where
 * this data ends up, and makes a loader testable by inspecting what it returns instead of by
 * observing side effects on three separate global registries.
 *
 * @param setName                    the set's full name, or null when the source did not supply one
 * @param cardTotal                  how many cards the set contains upstream — the denominator for
 *                                   {@link CardCatalog#getImplementedFraction}, and the one field
 *                                   covering the whole set rather than just implemented printings
 * @param rarityByCollectorNumber    rarity for every card in the set, implemented or not; the card
 *                                   browser shows unimplemented printings too
 * @param frontFaceByCollectorNumber oracle data for the implemented printings only, keyed by
 *                                   collector number. Absent entries mean the source had no card at
 *                                   that number.
 * @param backFaceByCollectorNumber  back-face oracle data, present only for printings that have a
 *                                   back face
 * @param tokenImages                token art keyed by {@link CardPrintingRegistry#buildTokenKey}.
 *                                   Empty when the set has no tokens or they could not be loaded —
 *                                   token failures are not set-load failures.
 */
public record SetOracleData(
        String setName,
        int cardTotal,
        Map<String, String> rarityByCollectorNumber,
        Map<String, OracleData> frontFaceByCollectorNumber,
        Map<String, OracleData> backFaceByCollectorNumber,
        Map<String, TokenImageData> tokenImages) {
}
