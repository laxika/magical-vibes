package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VillageBellRinger;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrimstoneVolley.class, GrizzlyBears.class, Shock.class, VillageBellRinger.class})
class BrimstoneVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player without morbid")
    void deals3DamageToPlayerWithoutMorbid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to target creature without morbid")
    void deals3DamageToCreatureWithoutMorbid() {
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        // 3 damage kills a 2/2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to target player with morbid")
    void deals5DamageToPlayerWithMorbid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        // Simulate a creature having died this turn
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deals 5 damage to target creature with morbid")
    void deals5DamageToCreatureWithMorbid() {
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Simulate a creature having died this turn
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Morbid triggers from opponent's creature dying too")
    void morbidTriggersFromOpponentCreatureDying() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        // Opponent's creature died this turn
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Should deal 5 (morbid) — doesn't matter whose creature died
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Morbid is checked at resolution time, not cast time")
    void morbidCheckedAtResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        // No creature has died when casting
        harness.castInstant(player1, 0, player2.getId());

        // Creature dies after casting but before resolution
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.passBothPriorities();

        // Should deal 5 since morbid is met at resolution time
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Killing a creature with Shock enables morbid for Brimstone Volley")
    void actualCreatureDeathEnablesMorbid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Cast Shock targeting Grizzly Bears (2 damage kills a 2/2)
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Bears should be dead
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Now cast Brimstone Volley targeting player2 — morbid should be active
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Should deal 5 damage (morbid), player2 started at 20
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Three damage does not kill a four-toughness creature or enable morbid")
    void nonlethalDamageDoesNotEnableMorbid() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new VillageBellRinger());
        harness.setHand(player1, List.of(new BrimstoneVolley(), new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Village Bell-Ringer"));
        harness.assertOnBattlefield(player2, "Village Bell-Ringer");
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Five damage kills a four-toughness creature with morbid")
    void morbidKillsFourToughnessCreature() {
        harness.addToBattlefield(player2, new VillageBellRinger());
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Village Bell-Ringer"));

        harness.assertNotOnBattlefield(player2, "Village Bell-Ringer");
        harness.assertInGraveyard(player2, "Village Bell-Ringer");
    }

    @Test
    @DisplayName("An actual creature death in response enables morbid at resolution")
    void creatureDiesInResponse() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrimstoneVolley(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }
}
