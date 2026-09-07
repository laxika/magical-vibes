package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolarBlast.class, GrizzlyBears.class})
class SolarBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SolarBlast()));
        addSpellMana(player1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to target creature")
    void deals3DamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SolarBlast()));
        addSpellMana(player1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling deals 1 damage to a chosen player and draws a card")
    void cyclingDealsDamageAndDraws() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Solar Blast");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling may deal no damage and still draws a card")
    void cyclingMayBeDeclined() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addSpellMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private void addCyclingMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
