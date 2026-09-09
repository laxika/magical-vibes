package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanYeti.class, BearCub.class, Forest.class})
class SylvanYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of cards in controller's hand; toughness stays 4")
    void powerEqualsHandSize() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of(new BearCub(), new BearCub(), new BearCub()));

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power is 0 with an empty hand; toughness stays 4")
    void powerZeroWithEmptyHand() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates dynamically as hand size changes")
    void powerUpdatesDynamically() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of());

        harness.setHand(player1, List.of(new BearCub()));
        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(1);

        harness.setHand(player1, List.of(new BearCub(), new BearCub()));
        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of(new BearCub()));
        harness.setHand(player2, List.of(new BearCub(), new BearCub()));

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power counts noncreature cards in controller's hand")
    void countsNoncreatureCardsInHand() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of(new Forest(), new BearCub()));

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(2);
    }
}
