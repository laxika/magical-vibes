package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResuscitateTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain the regeneration ability until end of turn")
    void grantsRegenerationAbilityToOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castResuscitate();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getRegenerationShield()).isEqualTo(1);
        assertThat(opponentCreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The granted regeneration ability wears off at end of turn")
    void grantedRegenerationAbilityWearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        castResuscitate();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ownCreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Resuscitate cannot grant an ability to a noncreature permanent")
    void doesNotGrantAbilityToNoncreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Pacifism());

        castResuscitate();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(noncreature.getRegenerationShield()).isZero();
    }

    private void castResuscitate() {
        harness.setHand(player1, List.of(new Resuscitate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

}
