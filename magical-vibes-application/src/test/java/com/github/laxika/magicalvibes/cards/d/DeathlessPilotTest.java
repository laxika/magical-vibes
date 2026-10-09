package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LumberingWorldwagon;
import com.github.laxika.magicalvibes.cards.v.VenomsacLagac;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathlessPilot.class, LumberingWorldwagon.class, VenomsacLagac.class})
class DeathlessPilotTest extends BaseCardTest {

    @Test
    @DisplayName("Its power bonus lets it crew a Vehicle")
    void powerBonusLetsItCrewVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new LumberingWorldwagon());
        Permanent pilot = addCreatureReady(player1, new DeathlessPilot());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick pilot can crew using its full power bonus")
    void summoningSickPilotCanCrewVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new LumberingWorldwagon());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new DeathlessPilot());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its saddle bonus applies even when its actual power is reduced")
    void reducedPowerPilotCanSaddleMount() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new VenomsacLagac());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new DeathlessPilot());
        pilot.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its graveyard ability returns it to its owner's hand")
    void returnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new DeathlessPilot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deathless Pilot");
        harness.assertNotInGraveyard(player1, "Deathless Pilot");
    }

    @Test
    @DisplayName("The graveyard ability returns only the activated copy")
    void returnsOnlyActivatedCopy() {
        harness.setHand(player1, List.of());
        DeathlessPilot activated = new DeathlessPilot();
        DeathlessPilot other = new DeathlessPilot();
        DeathlessPilot opponentCopy = new DeathlessPilot();
        harness.setGraveyard(player1, List.of(activated, other));
        harness.setGraveyard(player2, List.of(opponentCopy));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(activated);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCopy);
    }

    @Test
    @DisplayName("Multiple activations of one copy do not return another copy")
    void repeatedActivationsDoNotReturnOtherCopy() {
        harness.setHand(player1, List.of());
        DeathlessPilot activated = new DeathlessPilot();
        DeathlessPilot other = new DeathlessPilot();
        harness.setGraveyard(player1, List.of(activated, other));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(activated);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("The graveyard ability requires black mana")
    void cannotActivateWithoutBlackMana() {
        harness.setGraveyard(player1, List.of(new DeathlessPilot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Deathless Pilot");
        harness.assertNotInHand(player1, "Deathless Pilot");
    }
}
