package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HungryRidgewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/2 with no trample when alone")
    void noBoostWhenAlone() {
        harness.addToBattlefield(player1, new HungryRidgewolf());

        Permanent wolf = findPermanent(player1, "Hungry Ridgewolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("No boost with a non-Wolf, non-Werewolf creature")
    void noBoostWithIrrelevantCreature() {
        harness.addToBattlefield(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent wolf = findPermanent(player1, "Hungry Ridgewolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample when controller controls another Wolf")
    void boostWithAnotherWolf() {
        harness.addToBattlefield(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new WyluliWolf());

        Permanent wolf = findPermanent(player1, "Hungry Ridgewolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample when controller controls a Werewolf")
    void boostWithWerewolf() {
        harness.addToBattlefield(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new GreaterWerewolf());

        Permanent wolf = findPermanent(player1, "Hungry Ridgewolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Two Hungry Ridgewolves boost each other")
    void twoWolvesBoostEachOther() {
        harness.addToBattlefield(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new HungryRidgewolf());

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hungry Ridgewolf"))
                .toList();

        assertThat(wolves).hasSize(2);
        for (Permanent wolf : wolves) {
            assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent's Wolf does not grant the boost")
    void opponentWolfDoesNotCount() {
        harness.addToBattlefield(player1, new HungryRidgewolf());
        harness.addToBattlefield(player2, new WyluliWolf());

        Permanent wolf = findPermanent(player1, "Hungry Ridgewolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses boost when the other Wolf leaves the battlefield")
    void losesBoostWhenOtherWolfLeaves() {
        harness.addToBattlefield(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new WyluliWolf());

        Permanent wolf = findPermanent(player1, "Hungry Ridgewolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Wyluli Wolf"));

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new WyluliWolf());

        Permanent wolf = findPermanent(player1, "Hungry Ridgewolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);

        wolf.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }
}
