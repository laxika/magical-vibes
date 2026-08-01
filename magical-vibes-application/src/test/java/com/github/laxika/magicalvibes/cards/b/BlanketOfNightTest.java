package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService.StaticBonus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlanketOfNightTest extends BaseCardTest {

    @Test
    void allLandsGainSwampSubtype() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        Permanent ownForest = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent oppForest = gd.playerBattlefields.get(player2.getId()).getFirst();

        assertThat(gqs.computeStaticBonus(gd, ownForest).grantedSubtypes()).contains(CardSubtype.SWAMP);
        assertThat(gqs.computeStaticBonus(gd, oppForest).grantedSubtypes()).contains(CardSubtype.SWAMP);
    }

    @Test
    void landCanTapForBlack() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);
        // Forest's printed mana is ON_TAP; the granted Swamp ability is activated ability 0.
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void opponentLandCanTapForBlack() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player2, new Forest());

        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void nonLandsAreNotAffected() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        StaticBonus bonus = gqs.computeStaticBonus(gd, bears);

        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.SWAMP);
    }

    @Test
    void typeAndAbilityLostWhenBlanketLeaves() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).contains(CardSubtype.SWAMP);

        gd.playerBattlefields.get(player1.getId()).removeFirst();

        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).doesNotContain(CardSubtype.SWAMP);
        // Only the printed green ON_TAP mana remains.
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
