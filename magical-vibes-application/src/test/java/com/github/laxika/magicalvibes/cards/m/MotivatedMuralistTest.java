package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.InspiringStatuary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MotivatedMuralist.class, InspiringStatuary.class})
class MotivatedMuralistTest extends BaseCardTest {

    @Test
    void conjuresInspiringStatuaryIntoHand() {
        harness.castFromHand(player1, new MotivatedMuralist(), "{1}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player1, "Inspiring Statuary");
    }

    @Test
    void conjuresForItsControllerWhenEnteringWithoutBeingCast() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player2, new MotivatedMuralist());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Inspiring Statuary");
        harness.assertNotInHand(player1, "Inspiring Statuary");
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getOwnerId())
                .isEqualTo(player2.getId());
    }

    @Test
    void conjuredStatuaryCanBeCastAsARealCard() {
        harness.castFromHand(player1, new MotivatedMuralist(), "{1}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inspiring Statuary");
        harness.assertNotInHand(player1, "Inspiring Statuary");
    }

    @Test
    void canPayGenericManaByTappingAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        harness.setHand(player1, List.of(new MotivatedMuralist()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(artifact.getId()));
        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Motivated Muralist");
        harness.assertInHand(player1, "Inspiring Statuary");
    }

    @Test
    void improviseCannotPayTheRedManaRequirement() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        harness.setHand(player1, List.of(new MotivatedMuralist()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player1, 0, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Motivated Muralist");
        harness.assertNotInHand(player1, "Inspiring Statuary");
    }

    @Test
    void cannotImproviseWithAnAlreadyTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        artifact.tap();
        harness.setHand(player1, List.of(new MotivatedMuralist()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player1, 0, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Motivated Muralist");
    }

    @Test
    void cannotImproviseWithAnOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new InspiringStatuary());
        harness.setHand(player1, List.of(new MotivatedMuralist()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player1, 0, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Motivated Muralist");
    }
}
