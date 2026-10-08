package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopperhornScout.class, GrizzlyBears.class, Forest.class, GalvanicBlast.class})
class CopperhornScoutTest extends BaseCardTest {


    @Test
    @DisplayName("Attacking puts trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new CopperhornScout());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Copperhorn Scout");
    }


    @Test
    @DisplayName("Resolving trigger untaps each other tapped creature you control")
    void untapsOtherTappedCreatures() {
        addCreatureReady(player1, new CopperhornScout());
        Permanent bear1 = addCreatureReady(player1, new GrizzlyBears());
        bear1.tap();
        Permanent bear2 = addCreatureReady(player1, new GrizzlyBears());
        bear2.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap the Scout itself")
    void doesNotUntapSelf() {
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        // Scout attacked so it's tapped — should NOT be untapped by its own trigger
        assertThat(scout.isTapped()).isTrue();
        // Bear should be untapped
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        addCreatureReady(player1, new CopperhornScout());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        opponentBear.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not affect already-untapped creatures")
    void doesNotAffectAlreadyUntappedCreatures() {
        addCreatureReady(player1, new CopperhornScout());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        // bear is not tapped

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps another attacker without removing it from combat")
    void untapsAnotherAttacker() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_ATTACKERS));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.DECLARE_ATTACKERS));
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        assertThat(bear.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Two attacking Scouts untap one another")
    void twoScoutsUntapEachOther() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_ATTACKERS));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.DECLARE_ATTACKERS));
        Permanent first = addCreatureReady(player1, new CopperhornScout());
        Permanent second = addCreatureReady(player1, new CopperhornScout());

        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(first.isAttacking()).isTrue();
        assertThat(second.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Does not untap a noncreature land")
    void doesNotUntapLand() {
        addCreatureReady(player1, new CopperhornScout());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attack trigger still untaps creatures after the Scout dies")
    void resolvesAfterSourceDies() {
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent other = addCreatureReady(player1, new CopperhornScout());
        other.tap();
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(player1, List.of(0));
        harness.castInstant(player2, 0, scout.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scout);
        assertThat(other.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(other.isTapped()).isFalse();
    }
}
