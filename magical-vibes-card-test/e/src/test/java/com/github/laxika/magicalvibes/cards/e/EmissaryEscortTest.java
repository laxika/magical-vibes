package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.Hylderblade;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.Ouroboroid;
import com.github.laxika.magicalvibes.cards.s.SurveyMechan;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmissaryEscort.class, SurveyMechan.class, Hylderblade.class,
        MycosynthLattice.class, Ouroboroid.class})
class EmissaryEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Does not count itself")
    void doesNotCountItself() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());

        assertThat(gqs.getEffectivePower(gd, emissary)).isZero();
    }

    @Test
    @DisplayName("Gets power equal to the greatest other artifact mana value")
    void getsPowerFromGreatestOtherArtifactManaValue() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        harness.addToBattlefield(player1, new SurveyMechan());

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ignores artifacts controlled by an opponent")
    void ignoresOpponentArtifacts() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        harness.addToBattlefield(player2, new SurveyMechan());

        assertThat(gqs.getEffectivePower(gd, emissary)).isZero();
    }

    @Test
    @DisplayName("Updates when the greatest other artifact leaves")
    void updatesWhenArtifactLeaves() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        var artifact = harness.addToBattlefieldAndReturn(player1, new SurveyMechan());

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(artifact);

        assertThat(gqs.getEffectivePower(gd, emissary)).isZero();
    }

    @Test
    void usesGreatestValueRatherThanSumAndFallsBackWhenItLeaves() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        harness.addToBattlefield(player1, new Hylderblade());
        harness.addToBattlefield(player1, new EmissaryEscort());
        var greatest = harness.addToBattlefieldAndReturn(player1, new SurveyMechan());

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, emissary)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(greatest);

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(2);
    }

    @Test
    void ignoresNonartifactPermanentsWithGreaterManaValue() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        harness.addToBattlefield(player1, new Hylderblade());
        harness.addToBattlefield(player1, new Ouroboroid());

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(1);
    }

    @Test
    void countsPermanentsMadeArtifactsByOpponentsLattice() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        harness.addToBattlefield(player1, new Ouroboroid());
        harness.addToBattlefield(player2, new MycosynthLattice());

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(4);
    }
}
