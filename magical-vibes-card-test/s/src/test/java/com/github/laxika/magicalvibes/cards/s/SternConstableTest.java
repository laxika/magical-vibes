package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SternConstable.class, DevilthornFox.class, MagnifyingGlass.class})
class SternConstableTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability starts a discard-cost choice")
    void activationStartsDiscardChoice() {
        addCreatureReady(player1, new SternConstable());
        Permanent target = addCreatureReady(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new DevilthornFox()));

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Discarding a card taps the target creature")
    void discardTapsTargetCreature() {
        Permanent constable = addCreatureReady(player1, new SternConstable());
        Permanent target = addCreatureReady(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new DevilthornFox()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(constable.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Devilthorn Fox");
    }

    @Test
    @DisplayName("The ability rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        addCreatureReady(player1, new SternConstable());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MagnifyingGlass());
        harness.setHand(player1, List.of(new DevilthornFox()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap and discard costs are paid before the target is tapped")
    void costsArePaidBeforeResolution() {
        Permanent constable = addCreatureReady(player1, new SternConstable());
        Permanent target = addCreatureReady(player2, new SternConstable());
        harness.setHand(player1, List.of(new SternConstable(), new MagnifyingGlass()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(constable.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Magnifying Glass");
        harness.assertInHand(player1, "Stern Constable");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated with an empty hand")
    void cannotActivateWithEmptyHand() {
        Permanent constable = addCreatureReady(player1, new SternConstable());
        Permanent target = addCreatureReady(player2, new SternConstable());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(constable.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Constable cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent constable = addCreatureReady(player1, new SternConstable());
        constable.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new SternConstable());
        harness.setHand(player1, List.of(new SternConstable()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(constable.isTapped()).isFalse();
        harness.assertInHand(player1, "Stern Constable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already-tapped Constable cannot activate its tap ability")
    void cannotActivateWhileTapped() {
        Permanent constable = addCreatureReady(player1, new SternConstable());
        constable.tap();
        Permanent target = addCreatureReady(player2, new SternConstable());
        harness.setHand(player1, List.of(new SternConstable()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Stern Constable");
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Constable can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent constable = addCreatureReady(player1, new SternConstable());
        harness.setHand(player1, List.of(new SternConstable()));

        harness.activateAbility(player1, 0, null, constable.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(constable.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(constable.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Stern Constable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already-tapped creature is a legal target")
    void canTargetTappedCreature() {
        Permanent constable = addCreatureReady(player1, new SternConstable());
        Permanent target = addCreatureReady(player2, new SternConstable());
        target.tap();
        harness.setHand(player1, List.of(new SternConstable()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(constable.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Stern Constable");
        assertThat(gd.stack).isEmpty();
    }
}
