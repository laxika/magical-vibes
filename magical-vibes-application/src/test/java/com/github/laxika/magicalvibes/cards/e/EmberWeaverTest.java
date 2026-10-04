package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinOutlander;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberWeaver.class, GrizzlyBears.class, HillGiant.class, GoblinOutlander.class,
        EsperCormorants.class})
class EmberWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 and first strike while controller controls a red permanent")
    void boostedWithRedPermanent() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());
        harness.addToBattlefield(player1, new HillGiant()); // red permanent

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3); // 2 base + 1
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("No boost or first strike without a red permanent")
    void noBoostWithoutRedPermanent() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2); // base
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A non-red permanent does not grant the bonus")
    void nonRedPermanentDoesNotCount() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());
        harness.addToBattlefield(player1, new GrizzlyBears()); // green

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses +1/+0 and first strike when the red permanent leaves the battlefield")
    void losesBonusWhenRedPermanentLeaves() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());
        harness.addToBattlefield(player1, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getColors().contains(CardColor.RED));

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's red permanent does not grant the bonus")
    void opponentRedPermanentDoesNotCount() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());
        harness.addToBattlefield(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A multicolored red permanent enables the bonus without boosting other creatures")
    void multicoloredRedPermanentEnablesOnlyWeaversBonus() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());
        Permanent outlander = harness.addToBattlefieldAndReturn(player1, new GoblinOutlander());

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, weaver)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, outlander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, outlander, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Multiple red permanents grant the bonus only once and losing one keeps it active")
    void multipleRedPermanentsDoNotStack() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoblinOutlander());
        harness.addToBattlefield(player1, new GoblinOutlander());

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, weaver)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Red cards in hand and graveyard do not enable the bonus")
    void redCardsOutsideBattlefieldDoNotCount() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());
        harness.setHand(player1, List.of(new GoblinOutlander()));
        harness.setGraveyard(player1, List.of(new GoblinOutlander()));

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gaining a red permanent enables the bonus immediately")
    void gainsBonusWhenRedPermanentEnters() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new EmberWeaver());
        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();

        harness.addToBattlefield(player1, new GoblinOutlander());

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Reach blocks a flyer without controlling a red permanent")
    void blocksFlyerWithoutRedPermanent() {
        addCreatureReady(player1, new EsperCormorants()).setAttacking(true);
        addCreatureReady(player2, new EmberWeaver());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Ember Weaver");
        harness.assertOnBattlefield(player1, "Esper Cormorants");
    }

    @Test
    @DisplayName("With a red permanent first strike kills a flyer before it damages the Weaver")
    void firstStrikeKillsFlyerBeforeNormalDamage() {
        addCreatureReady(player1, new EsperCormorants()).setAttacking(true);
        addCreatureReady(player2, new EmberWeaver());
        harness.addToBattlefield(player2, new GoblinOutlander());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Ember Weaver");
        harness.assertInGraveyard(player1, "Esper Cormorants");
        assertThat(findPermanent(player2, "Ember Weaver").getMarkedDamage()).isZero();
    }
}
