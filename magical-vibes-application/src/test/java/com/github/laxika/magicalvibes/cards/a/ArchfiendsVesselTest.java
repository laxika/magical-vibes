package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YawgmothsAgenda;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchfiendsVesselTest extends BaseCardTest {

    @Test
    @DisplayName("A normal cast does not create a Demon")
    void normalCastDoesNotCreateDemon() {
        harness.setHand(player1, List.of(new ArchfiendsVessel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Archfiend's Vessel");
        assertThat(countPermanents(player1, "Demon")).isZero();
    }

    @Test
    @DisplayName("Casting it from a graveyard exiles it and creates a flying Demon")
    void castFromGraveyardExilesItAndCreatesDemon() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new ArchfiendsVessel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Archfiend's Vessel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Archfiend's Vessel"));
        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning it from a graveyard exiles it and creates a flying Demon")
    void returningFromGraveyardExilesItAndCreatesDemon() {
        var vessel = new ArchfiendsVessel();
        harness.setGraveyard(player1, List.of(vessel));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, vessel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Archfiend's Vessel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Archfiend's Vessel"));
        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
    }

    @Test
    @DisplayName("If it leaves before its trigger resolves, it does not create a Demon")
    void leavingBeforeTriggerResolutionDoesNotCreateDemon() {
        var vessel = new ArchfiendsVessel();
        harness.setGraveyard(player1, List.of(vessel));
        harness.setHand(player1, List.of(new Zombify(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, vessel.getId());
        harness.passBothPriorities();

        var vesselPermanentId = harness.getPermanentId(player1, "Archfiend's Vessel");
        harness.castInstant(player1, 0, vesselPermanentId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Archfiend's Vessel");
        assertThat(countPermanents(player1, "Demon")).isZero();
    }
}
