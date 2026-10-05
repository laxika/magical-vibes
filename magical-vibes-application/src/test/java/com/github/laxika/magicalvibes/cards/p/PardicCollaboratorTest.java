package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PardicCollaborator.class, BaskingRootwalla.class})
class PardicCollaboratorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Pardic Collaborator +1/+1 until end of turn")
    void abilityBoostsSelf() {
        Permanent collaborator = addCollaboratorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(collaborator.getEffectivePower()).isEqualTo(3);
        assertThat(collaborator.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly while black mana remains")
    void abilityCanBeActivatedRepeatedly() {
        Permanent collaborator = addCollaboratorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(collaborator.getEffectivePower()).isEqualTo(4);
        assertThat(collaborator.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability boosts only the Pardic Collaborator whose ability was activated")
    void abilityBoostsOnlyItsSource() {
        Permanent collaborator = addCollaboratorReady(player1);
        Permanent otherCollaborator = addCollaboratorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(collaborator.getEffectivePower()).isEqualTo(3);
        assertThat(collaborator.getEffectiveToughness()).isEqualTo(3);
        assertThat(otherCollaborator.getEffectivePower()).isEqualTo(2);
        assertThat(otherCollaborator.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability does not tap Pardic Collaborator")
    void abilityDoesNotTapSource() {
        Permanent collaborator = addCollaboratorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(collaborator.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off during cleanup")
    void boostWearsOffAtEndOfTurn() {
        Permanent collaborator = addCollaboratorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(collaborator.getEffectivePower()).isEqualTo(2);
        assertThat(collaborator.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot be activated without black mana")
    void cannotActivateWithoutBlackMana() {
        addCollaboratorReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The boost applies only when the activated ability resolves")
    void boostWaitsForResolution() {
        Permanent collaborator = addCollaboratorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(collaborator.getEffectivePower()).isEqualTo(2);
        assertThat(collaborator.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(collaborator.getEffectivePower()).isEqualTo(3);
        assertThat(collaborator.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Collaborator can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent collaborator = addCollaboratorReady(player1);
        collaborator.setSummoningSick(true);
        collaborator.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(collaborator.getEffectivePower()).isEqualTo(3);
        assertThat(collaborator.getEffectiveToughness()).isEqualTo(3);
        assertThat(collaborator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent collaborator = addCollaboratorReady(player1);
        addCreatureReady(player2, new BaskingRootwalla());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Pardic Collaborator");
        harness.assertInGraveyard(player2, "Basking Rootwalla");
        assertThat(collaborator.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    private Permanent addCollaboratorReady(Player player) {
        return addCreatureReady(player, new PardicCollaborator());
    }
}
