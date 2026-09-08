package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TrumpetingGnarr.class)
class TrumpetingGnarrTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating creates a 3/3 green Beast token")
    void mutatingCreatesBeastToken() {
        Permanent gnarr = addCreatureReady(player1, new TrumpetingGnarr());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, gnarr, List.of(gnarr.getCard()), player1.getId()));
        resolveAllTriggers();

        List<Permanent> beasts = findPermanents(player1, "Beast");
        assertThat(beasts).hasSize(1);
        Permanent beast = beasts.getFirst();
        assertThat(beast.getCard().getPower()).isEqualTo(3);
        assertThat(beast.getCard().getToughness()).isEqualTo(3);
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(beast.getCard().isToken()).isTrue();
    }
}
