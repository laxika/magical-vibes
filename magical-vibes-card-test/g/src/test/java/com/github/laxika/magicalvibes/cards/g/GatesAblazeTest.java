package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.t.TerritorialBoar;
import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GatesAblaze.class, RakdosGuildgate.class, TerritorialBoar.class, ConcordiaPegasus.class})
class GatesAblazeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of Gates you control to each creature")
    void dealsDamageEqualToControlledGateCount() {
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        harness.addToBattlefield(player1, new TerritorialBoar());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new ConcordiaPegasus());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();

        harness.assertNotOnBattlefield(player1, "Territorial Boar");
        assertThat(survivor.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent-controlled Gates do not increase the damage")
    void countsOnlyGatesYouControl() {
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ConcordiaPegasus());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ConcordiaPegasus());

        cast();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals no damage when you control no Gates")
    void dealsNoDamageWithoutGates() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ConcordiaPegasus());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ConcordiaPegasus());

        cast();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Counts Gates at resolution rather than when the spell is cast")
    void countsGatesAtResolution() {
        harness.addToBattlefield(player1, new RakdosGuildgate());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ConcordiaPegasus());
        harness.setHand(player1, List.of(new GatesAblaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Concordia Pegasus");
    }

    @Test
    @DisplayName("Lethal damage puts creatures from both players into their graveyards")
    void lethalDamageKillsCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new TerritorialBoar());
        harness.addToBattlefield(player2, new ConcordiaPegasus());

        cast();

        harness.assertNotOnBattlefield(player1, "Territorial Boar");
        harness.assertNotOnBattlefield(player2, "Concordia Pegasus");
        harness.assertInGraveyard(player1, "Territorial Boar");
        harness.assertInGraveyard(player2, "Concordia Pegasus");
        harness.assertOnBattlefield(player1, "Rakdos Guildgate");
        harness.assertInGraveyard(player1, "Gates Ablaze");
    }

    private void cast() {
        harness.setHand(player1, List.of(new GatesAblaze()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
