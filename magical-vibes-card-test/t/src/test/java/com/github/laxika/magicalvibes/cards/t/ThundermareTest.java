package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thundermare.class, GrizzlyBears.class, Millstone.class})
class ThundermareTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all other creatures on both battlefields")
    void etbTapsAllOtherCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap Thundermare itself")
    void etbDoesNotTapSelf() {
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Thundermare").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste allows Thundermare to attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        Permanent thundermare = findPermanent(player1, "Thundermare");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(thundermare)));

        assertThat(thundermare.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap other noncreature permanents")
    void etbDoesNotTapNoncreatures() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());

        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        assertThat(millstone.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB taps another Thundermare but not the one entering")
    void etbExcludesOnlyTheEnteringThundermare() {
        Permanent existingThundermare = addCreatureReady(player1, new Thundermare());
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        List<Permanent> thundermarePermanents = findPermanents(player1, "Thundermare");
        assertThat(thundermarePermanents).hasSize(2);
        assertThat(existingThundermare.isTapped()).isTrue();
        assertThat(thundermarePermanents.get(1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB taps creatures present at resolution, including ones added after it triggers")
    void etbUsesCreaturesPresentAtResolution() {
        Permanent existingCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(existingCreature.isTapped()).isFalse();
        Permanent newCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveAllTriggers();

        assertThat(existingCreature.isTapped()).isTrue();
        assertThat(newCreature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Thundermare").isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB leaves already tapped creatures and a tapped source tapped")
    void etbDoesNotUntapTheSourceOrOtherCreatures() {
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        otherCreature.tap();
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        Permanent thundermare = findPermanent(player1, "Thundermare");
        thundermare.tap();

        resolveAllTriggers();

        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(thundermare.isTapped()).isTrue();
    }
}
