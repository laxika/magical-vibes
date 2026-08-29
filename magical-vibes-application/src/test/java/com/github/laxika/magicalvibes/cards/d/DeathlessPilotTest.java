package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FellFlagship;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathlessPilotTest extends BaseCardTest {

    @Test
    @DisplayName("Its power bonus lets it crew a Vehicle")
    void powerBonusLetsItCrewVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FellFlagship());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new DeathlessPilot());
        pilot.setSummoningSick(false);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
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
}
