package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cosmogoyf.class, Forest.class, GrizzlyBears.class, Opt.class})
class CosmogoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Cosmogoyf is 0/1 when its controller owns no cards in exile")
    void isZeroOneWithEmptyExile() {
        Permanent cosmogoyf = addCreatureReady(player1, new Cosmogoyf());

        assertStats(cosmogoyf, 0, 1);
    }

    @Test
    @DisplayName("Power counts cards its controller owns in exile and toughness is one greater")
    void countsOwnExiledCards() {
        Permanent cosmogoyf = addCreatureReady(player1, new Cosmogoyf());
        harness.setExile(player1, List.of(new Forest(), new GrizzlyBears(), new Opt()));
        harness.setExile(player2, List.of(new Forest(), new GrizzlyBears()));

        assertStats(cosmogoyf, 3, 4);
    }

    @Test
    @DisplayName("Power and toughness update as owned exiled cards leave")
    void updatesWhenExiledCardLeaves() {
        Permanent cosmogoyf = addCreatureReady(player1, new Cosmogoyf());
        Forest exiledCard = new Forest();
        harness.setExile(player1, List.of(exiledCard, new Opt()));

        assertStats(cosmogoyf, 2, 3);

        gd.removeFromExile(exiledCard.getId());

        assertStats(cosmogoyf, 1, 2);
    }

    private void assertStats(Permanent cosmogoyf, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, cosmogoyf)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, cosmogoyf)).isEqualTo(toughness);
    }
}
