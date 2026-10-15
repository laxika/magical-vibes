package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BaneOfHanweir;
import com.github.laxika.magicalvibes.cards.c.CatharCommando;
import com.github.laxika.magicalvibes.cards.r.RuneboundWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HungryRidgewolf.class, CatharCommando.class, RuneboundWolf.class,
        HanweirWatchkeep.class, BaneOfHanweir.class})
class HungryRidgewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/2 with no trample when alone")
    void noBoostWhenAlone() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("No boost with a non-Wolf, non-Werewolf creature")
    void noBoostWithIrrelevantCreature() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new CatharCommando());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample when controller controls another Wolf")
    void boostWithAnotherWolf() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new RuneboundWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample when controller controls a Werewolf")
    void boostWithWerewolf() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new HanweirWatchkeep());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Two Hungry Ridgewolves boost each other")
    void twoWolvesBoostEachOther() {
        harness.addToBattlefield(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new HungryRidgewolf());

        List<Permanent> wolves = findPermanents(player1, "Hungry Ridgewolf");

        assertThat(wolves).hasSize(2);
        for (Permanent wolf : wolves) {
            assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent's Wolf does not grant the boost")
    void opponentWolfDoesNotCount() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());
        harness.addToBattlefield(player2, new RuneboundWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses boost when the other Wolf leaves the battlefield")
    void losesBoostWhenOtherWolfLeaves() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new RuneboundWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Runebound Wolf"));

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new RuneboundWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);

        wolf.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Multiple qualifying creatures grant only one boost")
    void multipleQualifyingCreaturesDoNotStackBoost() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());
        harness.addToBattlefield(player1, new RuneboundWolf());
        harness.addToBattlefield(player1, new HanweirWatchkeep());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost follows control of the supporting Wolf")
    void boostUpdatesWhenSupportingWolfChangesController() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HungryRidgewolf());
        Permanent support = harness.addToBattlefieldAndReturn(player2, new RuneboundWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(support);
        gd.playerBattlefields.get(player1.getId()).add(support);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(support);
        gd.playerBattlefields.get(player2.getId()).add(support);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }
}
