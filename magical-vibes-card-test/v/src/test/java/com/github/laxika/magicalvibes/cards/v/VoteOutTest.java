package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSwarm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoteOut.class, GrizzlyBears.class, Forest.class, BladeOfTheSwarm.class})
class VoteOutTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature")
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VoteOut()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Convoke taps a creature and pays part of the cost")
    void convokeTapsCreatureAndPaysPartOfCost() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VoteOut()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(convoker.getId()));
        harness.passBothPriorities();

        assertThat(convoker.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new VoteOut()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick black creature can convoke the black mana cost and be the target")
    void convokesBlackCostWithOwnTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSwarm());
        target.setSummoningSick(true);
        harness.setHand(player1, List.of(new VoteOut()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(target.getId()));
        assertThat(target.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blade of the Swarm");
        harness.assertInGraveyard(player1, "Blade of the Swarm");
        harness.assertInGraveyard(player1, "Vote Out");
    }

    @Test
    @DisplayName("Four creatures can convoke the entire cost without mana")
    void convokesEntireCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSwarm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSwarm());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSwarm());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSwarm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSwarm());
        harness.setHand(player1, List.of(new VoteOut()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));
        harness.passBothPriorities();

        assertThat(List.of(first, second, third, fourth)).allMatch(Permanent::isTapped);
        harness.assertNotOnBattlefield(player2, "Blade of the Swarm");
        harness.assertInGraveyard(player2, "Blade of the Swarm");
    }

    @Test
    @DisplayName("A tapped creature cannot convoke")
    void tappedCreatureCannotConvoke() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSwarm());
        convoker.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSwarm());
        harness.setHand(player1, List.of(new VoteOut()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Blade of the Swarm");
    }
}
