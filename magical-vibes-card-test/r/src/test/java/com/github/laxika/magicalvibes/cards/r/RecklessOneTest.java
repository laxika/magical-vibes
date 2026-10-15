package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.n.NosyGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessOne.class, NosyGoblin.class, ElvishWarrior.class})
class RecklessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Goblins on the battlefield")
    void ptEqualsBattlefieldGoblinCount() {
        Permanent recklessOne = addCreatureReady(player1, new RecklessOne());
        harness.addToBattlefield(player1, new NosyGoblin());
        harness.addToBattlefield(player2, new NosyGoblin());
        harness.addToBattlefield(player1, new ElvishWarrior());

        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update when Goblins enter and leave the battlefield")
    void ptUpdatesWithGoblinCount() {
        Permanent recklessOne = addCreatureReady(player1, new RecklessOne());
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(1);

        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new NosyGoblin());
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(goblin);
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reckless One can attack on the turn it enters")
    void canAttackImmediatelyAfterResolving() {
        harness.castFromHand(player1, new RecklessOne(), "{3}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Power and toughness in hand and graveyard count only battlefield Goblins")
    void ptIsDefinedOutsideBattlefield() {
        RecklessOne inHand = new RecklessOne();
        RecklessOne inGraveyard = new RecklessOne();
        harness.setHand(player1, List.of(inHand, new NosyGoblin()));
        harness.setGraveyard(player2, List.of(inGraveyard, new NosyGoblin()));
        harness.addToBattlefield(player1, new ElvishWarrior());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isZero();

        harness.addToBattlefield(player1, new NosyGoblin());
        harness.addToBattlefield(player2, new NosyGoblin());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Reckless Ones count each other across controllers")
    void multipleCopiesCountEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RecklessOne());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RecklessOne());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }
}
