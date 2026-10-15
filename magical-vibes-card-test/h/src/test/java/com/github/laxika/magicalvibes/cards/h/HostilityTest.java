package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.d.DrownerOfSecrets;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hostility.class, LightningBolt.class, Lignify.class, Tarfire.class, DrownerOfSecrets.class})
class HostilityTest extends BaseCardTest {

    @Test
    @DisplayName("A spell you control that would damage an opponent is prevented; you get a token per damage")
    void preventsSpellDamageToOpponentAndCreatesTokens() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Hostility());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // The 3 damage to the opponent is prevented...
        harness.assertLife(player2, 20);
        // ...and Hostility's controller gets three 3/1 Elemental Shaman tokens.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Elemental Shaman"))
                .hasSize(3);
    }

    @Test
    @DisplayName("A spell an opponent controls is not affected: Hostility's controller still takes the damage")
    void doesNotPreventOpponentSpellDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Hostility());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        // Not "a spell you control" — damage goes through and no tokens are made.
        harness.assertLife(player1, 17);
        harness.assertNotOnBattlefield(player1, "Elemental Shaman");
    }

    @Test
    @DisplayName("When Hostility is put into the graveyard, a triggered ability shuffles it into its library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player1, new java.util.ArrayList<>());
        Permanent hostility = harness.addToBattlefieldAndReturn(player1, new Hostility());
        hostility.setMarkedDamage(6);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Hostility");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Hostility");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hostility"));
    }

    @Test
    void doesNotPreventDamageAfterLignifyRemovesAbilities() {
        Permanent hostility = harness.addToBattlefieldAndReturn(player1, new Hostility());
        harness.setHand(player2, List.of(new Lignify()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player2, 0, hostility.getId());
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Elemental Shaman");
    }

    @Test
    void stillShufflesAfterDyingWhileEnchantedByLignify() {
        harness.setLibrary(player1, List.of());
        Permanent hostility = harness.addToBattlefieldAndReturn(player1, new Hostility());
        harness.setHand(player2, List.of(new Lignify()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player2, 0, hostility.getId());
        harness.passBothPriorities();

        hostility.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Hostility");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Hostility");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hostility.getCard());
    }

    @Test
    void doesNotPreventOwnSpellDamageToSelf() {
        harness.addToBattlefield(player1, new Hostility());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Elemental Shaman");
    }

    @Test
    void doesNotPreventSpellDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new Hostility());
        Permanent opposingHostility = harness.addToBattlefieldAndReturn(player2, new Hostility());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, opposingHostility.getId());

        assertThat(opposingHostility.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Elemental Shaman");
    }

    @Test
    void multipleHostilitiesDoNotMultiplyTokens() {
        harness.addToBattlefield(player1, new Hostility());
        harness.addToBattlefield(player1, new Hostility());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Elemental Shaman"))
                .hasSize(2);
    }

    @Test
    void milledHostilityShufflesIntoItsOwnersLibrary() {
        Hostility hostility = new Hostility();
        harness.setLibrary(player1, List.of(hostility));
        addCreatureReady(player2, new DrownerOfSecrets());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hostility");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Hostility");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hostility);
    }
}
