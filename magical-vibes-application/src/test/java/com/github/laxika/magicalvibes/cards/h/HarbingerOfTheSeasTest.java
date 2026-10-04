package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarbingerOfTheSeas.class, Forest.class, Glimmerpost.class, AshayaSoulOfTheWild.class})
class HarbingerOfTheSeasTest extends BaseCardTest {

    @Test
    @DisplayName("Nonbasic land taps for blue instead of its normal mana")
    void nonbasicLandProducesBlue() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player1, new HarbingerOfTheSeas());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Basic land is unaffected")
    void basicLandUnaffected() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new HarbingerOfTheSeas());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nonbasic land resumes its normal mana after Harbinger of the Seas leaves")
    void normalManaResumesWhenHarbingerLeaves() {
        harness.addToBattlefield(player1, new Glimmerpost());
        Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new HarbingerOfTheSeas());

        gd.playerBattlefields.get(player1.getId()).remove(harbinger);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent's nonbasic land also produces blue mana")
    void opponentNonbasicLandProducesBlue() {
        harness.addToBattlefield(player2, new Glimmerpost());
        harness.addToBattlefield(player1, new HarbingerOfTheSeas());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Nonbasic lands lose their previous land types")
    void replacesLocusWithIsland() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Glimmerpost());
        harness.addToBattlefield(player1, new HarbingerOfTheSeas());

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ISLAND)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.LOCUS)).isFalse();
    }

    @Test
    @DisplayName("A nonbasic land entering under Harbinger loses its printed enter trigger")
    void enteringNonbasicLandDoesNotTriggerPrintedAbility() {
        harness.addToBattlefield(player1, new HarbingerOfTheSeas());
        harness.setHand(player1, List.of(new Glimmerpost()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Harbinger also becomes an Island when Ashaya makes it a nonbasic land")
    void affectsItselfWhenItBecomesANonbasicLand() {
        Permanent ashaya = harness.addToBattlefieldAndReturn(player1, new AshayaSoulOfTheWild());
        ashaya.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new HarbingerOfTheSeas());

        assertThat(gqs.hasEffectiveSubtype(gd, harbinger, CardSubtype.ISLAND)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, harbinger, CardSubtype.FOREST)).isFalse();
        assertThat(gqs.hasLostPrintedAbilities(gd, harbinger)).isTrue();
    }
}
