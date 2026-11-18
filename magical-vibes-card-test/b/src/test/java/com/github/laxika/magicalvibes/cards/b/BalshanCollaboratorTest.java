package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BalshanCollaborator.class)
class BalshanCollaboratorTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: Balshan Collaborator gets +1/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent collaborator = addCreatureReady(player1, new BalshanCollaborator());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, collaborator)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, collaborator)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Balshan Collaborator's boost stacks when activated repeatedly")
    void repeatedActivationsStack() {
        Permanent collaborator = addCreatureReady(player1, new BalshanCollaborator());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, collaborator)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, collaborator)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability does not require Balshan Collaborator to be untapped")
    void abilityDoesNotRequireTapping() {
        Permanent collaborator = addCreatureReady(player1, new BalshanCollaborator());
        collaborator.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, collaborator)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, collaborator)).isEqualTo(1);
        assertThat(collaborator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Balshan Collaborator's ability requires black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new BalshanCollaborator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Balshan Collaborator's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent collaborator = addCreatureReady(player1, new BalshanCollaborator());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, collaborator)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, collaborator)).isZero();
    }

    @Test
    @DisplayName("Balshan Collaborator can activate its ability while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent collaborator = harness.addToBattlefieldAndReturn(player1, new BalshanCollaborator());
        collaborator.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, collaborator)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, collaborator)).isEqualTo(1);
        assertThat(collaborator.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost applies only to its source and only when the ability resolves")
    void boostsOnlySourceOnResolution() {
        Permanent collaborator = addCreatureReady(player1, new BalshanCollaborator());
        Permanent otherCollaborator = addCreatureReady(player1, new BalshanCollaborator());
        Permanent opposingCollaborator = addCreatureReady(player2, new BalshanCollaborator());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(PermanentGrants.powerBoostOf(gd, collaborator)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, collaborator)).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, collaborator)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, collaborator)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, otherCollaborator)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherCollaborator)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposingCollaborator)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposingCollaborator)).isZero();
    }
}
