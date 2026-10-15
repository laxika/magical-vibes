package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpikedCorridorTorturePit.class, LightningBolt.class})
class SpikedCorridorTorturePitTest extends BaseCardTest {

    @Test
    void spikedCorridorCreatesThreeDevils() {
        castRoom(0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.DEVIL)).hasSize(3);
    }

    @Test
    void torturePitAddsTwoToNoncombatDamageToAnOpponent() {
        castRoom(1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void lockedTorturePitDoesNotIncreaseDamage() {
        castRoom(0);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    void torturePitDoesNotCreateDevilsUntilSpikedCorridorIsUnlocked() {
        Permanent room = castRoom(1);
        assertThat(findPermanents(player1, "Devil")).isEmpty();
        harness.addMana(player1, ManaColor.RED, 4);

        gs.unlockRoomDoor(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Devil")).hasSize(3);
    }

    @Test
    void unlockingTorturePitDoesNotCreateMoreDevils() {
        Permanent room = castRoom(0);
        harness.addMana(player1, ManaColor.RED, 4);
        gs.unlockRoomDoor(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);

        assertThat(findPermanents(player1, "Devil")).hasSize(3);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 15);
    }

    @Test
    void torturePitDoesNotIncreaseDamageToItsController() {
        castRoom(1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    void torturePitDoesNotIncreaseAnOpponentsSourceDamage() {
        castRoom(1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    void devilDeathDealsOneDamageWithoutTorturePit() {
        castRoom(0);
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        devil.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Devil")).hasSize(2);
    }

    @Test
    void torturePitIncreasesDevilDeathDamageToAnOpponent() {
        Permanent room = castRoom(0);
        harness.addMana(player1, ManaColor.RED, 4);
        gs.unlockRoomDoor(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        devil.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void torturePitDoesNotIncreaseCombatDamage() {
        Permanent room = castRoom(0);
        harness.addMana(player1, ManaColor.RED, 4);
        gs.unlockRoomDoor(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        devil.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(devil)));
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new SpikedCorridorTorturePit()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        if (doorIndex == 0) {
            harness.passBothPriorities();
        }
        return findPermanent(player1, "Spiked Corridor // Torture Pit");
    }
}
