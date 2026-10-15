package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifeChisel.class, GrizzlyBears.class})
class LifeChiselTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gains life equal to its toughness")
    void sacrificeCreatureGainsLifeEqualToToughness() {
        harness.addToBattlefield(player1, new LifeChisel());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller chooses which creature to sacrifice")
    void choosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new LifeChisel());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(secondCreature);
    }

    @Test
    @DisplayName("The ability cannot be activated during an opponent's upkeep")
    void abilityRequiresControllerUpkeep() {
        harness.addToBattlefield(player1, new LifeChisel());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability can only be activated during its controller's upkeep")
    void abilityRequiresYourUpkeep() {
        harness.addToBattlefield(player1, new LifeChisel());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The sacrifice is paid immediately and uses modified toughness")
    void sacrificeUsesToughnessAtPayment() {
        harness.addToBattlefield(player1, new LifeChisel());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        advanceToUpkeep(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each activation retains the toughness of its own sacrificed creature")
    void repeatedActivationsRetainSeparateToughness() {
        harness.addToBattlefield(player1, new LifeChisel());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 20);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.assertLife(player1, 27);
        harness.assertOnBattlefield(player1, "Life Chisel");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotActivateWithoutCreatureYouControl() {
        harness.addToBattlefield(player1, new LifeChisel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
