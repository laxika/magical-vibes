package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Abduction;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravebaneZombie.class, WrathOfGod.class, Abduction.class, Counterspell.class,
        Humble.class, Unsummon.class})
class GravebaneZombieTest extends BaseCardTest {

    @Test
    @DisplayName("When Gravebane Zombie would die, it is put on top of its owner's library instead")
    void putOnTopOfLibraryInsteadOfDying() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());

        destroyWithWrathOfGod(player2);

        // Not on battlefield, and NOT in the graveyard — replacement effect applied
        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");

        // Placed on TOP of its owner's library (index 0), above the pre-existing card
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).containsExactly(zombie.getCard(), filler);

        // Log confirms the replacement
        assertThat(gameLogContains("Gravebane Zombie is put on top of its owner's library instead of dying."))
                .isTrue();
    }

    @Test
    @DisplayName("When controlled by another player, it is put on its owner's library")
    void putsOnOwnersLibraryWhenControlledByAnotherPlayer() {
        Card ownerFiller = new WrathOfGod();
        Card controllerFiller = new WrathOfGod();
        harness.setLibrary(player1, List.of(ownerFiller));
        harness.setLibrary(player2, List.of(controllerFiller));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());

        harness.setHand(player2, List.of(new Abduction()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, zombie.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        destroyWithWrathOfGod(player2);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), ownerFiller);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerFiller);
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player2, "Gravebane Zombie");
    }

    @Test
    @DisplayName("Lethal damage also puts it on top of its owner's library")
    void putsOnTopWhenStateBasedActionsWouldMakeItDie() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        zombie.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), filler);
    }

    @Test
    @DisplayName("A countered Gravebane Zombie goes to the graveyard, not the library")
    void counteredSpellGoesToGraveyard() {
        Card zombie = new GravebaneZombie();
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        harness.castFromHand(player1, zombie, "{3}{B}");
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, zombie.getId());

        harness.assertInGraveyard(player1, "Gravebane Zombie");
        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
    }

    @Test
    @DisplayName("Returning Gravebane Zombie to hand does not apply its death replacement")
    void returnedToHandInsteadOfLibrary() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, zombie.getId());

        harness.assertInHand(player1, "Gravebane Zombie");
        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
    }

    @Test
    @DisplayName("Gravebane Zombie goes to the graveyard when destroyed after losing its abilities")
    void diesNormallyAfterLosingAbilities() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, zombie.getId());

        destroyWithWrathOfGod(player2);

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertInGraveyard(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
    }

    private void destroyWithWrathOfGod(Player caster) {
        harness.forceActivePlayer(caster);
        harness.castFromHand(caster, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
