package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DragonHatchling;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirespitterWhelp.class, DragonHatchling.class, Divination.class, GrizzlyBears.class, Shock.class})
class FirespitterWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell deals 1 damage to each opponent")
    void noncreatureSpellDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting a Dragon creature spell deals 1 damage to each opponent")
    void dragonSpellDealsDamage() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player1, List.of(new DragonHatchling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a non-Dragon creature spell does not trigger Firespitter Whelp")
    void nonDragonCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting Firespitter Whelp does not trigger its own ability")
    void doesNotTriggerForItsOwnCast() {
        harness.setHand(player1, List.of(new FirespitterWhelp()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Firespitter Whelp");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only the casting player's Whelp triggers")
    void opponentsWhelpDoesNotTrigger() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.addToBattlefield(player2, new FirespitterWhelp());
        harness.setHand(player1, List.of(new FirespitterWhelp()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Each Whelp triggers separately for the same spell")
    void multipleWhelpsEachTrigger() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player1, List.of(new FirespitterWhelp()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Whelp")
    void opponentsNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Dragon spell does not trigger Whelp")
    void opponentsDragonSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FirespitterWhelp()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A pending trigger deals damage even after Whelp dies")
    void triggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player1, List.of(new FirespitterWhelp()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Firespitter Whelp"));

        harness.assertNotOnBattlefield(player1, "Firespitter Whelp");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
