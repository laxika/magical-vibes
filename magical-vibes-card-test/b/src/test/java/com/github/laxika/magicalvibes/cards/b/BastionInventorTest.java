package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BastionInventor.class, BarricadeBreaker.class, AetherChaser.class, Shock.class})
class BastionInventorTest extends BaseCardTest {

    @Test
    @DisplayName("Improvise taps an artifact to pay generic mana")
    void improviseTapsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        harness.setHand(player1, List.of(new BastionInventor()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bastion Inventor");
    }

    @Test
    @DisplayName("Improvise cannot tap a nonartifact permanent")
    void improviseRejectsNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AetherChaser());
        harness.setHand(player1, List.of(new BastionInventor()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void improviseCanTapSummoningSickArtifactCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        artifact.setSummoningSick(true);
        harness.setHand(player1, List.of(new BastionInventor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Bastion Inventor");
    }

    @Test
    void improviseRejectsTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        artifact.tap();
        harness.setHand(player1, List.of(new BastionInventor()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Bastion Inventor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseCannotPayBlueMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        harness.setHand(player1, List.of(new BastionInventor()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Bastion Inventor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseRejectsRepeatedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        harness.setHand(player1, List.of(new BastionInventor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Bastion Inventor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hexproofRejectsOpponentsSpell() {
        Permanent inventor = harness.addToBattlefieldAndReturn(player1, new BastionInventor());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, inventor.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertInHand(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hexproofAllowsControllersSpell() {
        Permanent inventor = harness.addToBattlefieldAndReturn(player1, new BastionInventor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, inventor.getId());
        harness.passBothPriorities();

        assertThat(inventor.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Bastion Inventor");
        harness.assertInGraveyard(player1, "Shock");
    }
}
