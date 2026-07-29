package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonOracleLoader;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the back face's printing identity: both faces of a double-faced card share one printing,
 * and the client fetches art by set code and collector number, so a back face without them cannot
 * be drawn at all — a transformed permanent renders with an empty art window (the Ulvenwald
 * Primordials bug, July 2026).
 *
 * <p>The identity is stamped by {@link CardPrinting#createCard()} and only there. Card classes
 * build their back face inside their own constructor, which runs before the printing identity
 * exists, so any {@code backFace.setSetCode(getSetCode())} written there copies null.
 */
@Tag("scryfall")
class BackFacePrintingIdentityTest {

    private static final String CACHE_DIR = "./card-data-cache";

    @Test
    void everyBackFaceCarriesItsPrintingIdentity() {
        Card.clearOracleRegistry();
        CardRegistry registry = new CardRegistry(new MtgjsonOracleLoader(CACHE_DIR));
        registry.load();

        List<String> violations = new ArrayList<>();
        int backFacesChecked = 0;

        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : registry.getPrintings(set)) {
                Card back = printing.createCard().getBackFaceCard();
                if (back == null) {
                    continue;
                }
                backFacesChecked++;

                String label = back.getName() + " [" + back.getClass().getSimpleName() + ", back face of "
                        + set.getCode() + " #" + printing.collectorNumber() + "]";
                if (!printing.setCode().equals(back.getSetCode())
                        || !printing.collectorNumber().equals(back.getCollectorNumber())) {
                    violations.add(label + ": got " + back.getSetCode() + " #" + back.getCollectorNumber());
                }
            }
        }

        assertThat(backFacesChecked).as("sanity: registered printings with a back face found").isGreaterThan(80);
        assertThat(violations)
                .as("back faces whose set code and collector number do not match their printing")
                .isEmpty();
    }
}
