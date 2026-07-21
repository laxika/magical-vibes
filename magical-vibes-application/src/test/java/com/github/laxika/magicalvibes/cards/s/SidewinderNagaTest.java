package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SidewinderNagaTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/2 with no trample when no Desert")
    void baseStatsWithoutDesert() {
        harness.addToBattlefield(player1, new SidewinderNaga());

        Permanent naga = findNaga();
        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample while you control a Desert")
    void boostAndTrampleWithDesertOnBattlefield() {
        harness.addToBattlefield(player1, new SidewinderNaga());
        harness.addToBattlefield(player1, new SunscorchedDesert());

        Permanent naga = findNaga();
        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample while a Desert card is in your graveyard")
    void boostAndTrampleWithDesertInGraveyard() {
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        harness.addToBattlefield(player1, new SidewinderNaga());

        Permanent naga = findNaga();
        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Battlefield and graveyard Deserts do not stack the boost")
    void bothDesertsDoNotStack() {
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        harness.addToBattlefield(player1, new SidewinderNaga());
        harness.addToBattlefield(player1, new SunscorchedDesert());

        Permanent naga = findNaga();
        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Desert does not grant the boost or trample")
    void opponentDesertDoesNotCount() {
        harness.addToBattlefield(player1, new SidewinderNaga());
        harness.addToBattlefield(player2, new SunscorchedDesert());

        Permanent naga = findNaga();
        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's graveyard Desert does not grant the boost or trample")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new SunscorchedDesert()));
        harness.addToBattlefield(player1, new SidewinderNaga());

        Permanent naga = findNaga();
        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses boost and trample when its only Desert leaves the battlefield")
    void losesWhenDesertLeaves() {
        harness.addToBattlefield(player1, new SidewinderNaga());
        harness.addToBattlefield(player1, new SunscorchedDesert());

        Permanent naga = findNaga();
        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sunscorched Desert"));

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A non-Desert permanent does not grant the boost or trample")
    void nonDesertDoesNotCount() {
        harness.addToBattlefield(player1, new SidewinderNaga());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent naga = findNaga();
        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent findNaga() {
        return findPermanent(player1, "Sidewinder Naga");
    }
}
