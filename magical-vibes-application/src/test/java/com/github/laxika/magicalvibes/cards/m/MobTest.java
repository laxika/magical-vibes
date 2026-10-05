package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChangelingOutcast;
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

@CardUsed({Mob.class, GrizzlyBears.class, Forest.class, ChangelingOutcast.class})
class MobTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target creature")
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mob()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mob");
    }

    @Test
    @DisplayName("Convoke taps creatures to help pay for Mob")
    void castsWithConvoke() {
        Permanent firstConvoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mob()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(firstConvoker.getId(), secondConvoker.getId()));
        harness.passBothPriorities();

        assertThat(firstConvoker.isTapped()).isTrue();
        assertThat(secondConvoker.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Black convokers can pay the entire cost even with summoning sickness")
    void castsWithOnlySummoningSickBlackCreatures() {
        List<Permanent> convokers = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new ChangelingOutcast()))
                .toList();
        convokers.forEach(p -> p.setSummoningSick(true));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChangelingOutcast());
        harness.setHand(player1, List.of(new Mob()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                convokers.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(convokers).allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player2, "Changeling Outcast");
        harness.assertInGraveyard(player1, "Mob");
    }

    @Test
    @DisplayName("Green convokers cannot pay the black mana requirement")
    void greenCreaturesCannotPayBlackRequirement() {
        List<Permanent> convokers = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()))
                .toList();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mob()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                convokers.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mob");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tapped creature cannot convoke")
    void cannotConvokeWithTappedCreature() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new ChangelingOutcast());
        convoker.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChangelingOutcast());
        harness.setHand(player1, List.of(new Mob()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mob");
        harness.assertOnBattlefield(player2, "Changeling Outcast");
    }

    @Test
    @DisplayName("Can convoke with the creature being destroyed")
    void canConvokeWithOwnTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ChangelingOutcast());
        harness.setHand(player1, List.of(new Mob()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(target.getId()));
        assertThat(target.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Changeling Outcast");
        harness.assertInGraveyard(player1, "Mob");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Mob()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
