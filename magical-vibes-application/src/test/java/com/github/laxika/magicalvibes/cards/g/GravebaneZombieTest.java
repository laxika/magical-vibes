package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PhyrexianPurge;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
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

@CardUsed({GravebaneZombie.class, PhyrexianPurge.class})
class GravebaneZombieTest extends BaseCardTest {

    @Test
    @DisplayName("When Gravebane Zombie would die, it is put on top of its owner's library instead")
    void putOnTopOfLibraryInsteadOfDying() {
        Card filler = new PhyrexianPurge();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());

        destroyWithPhyrexianPurge(player2, zombie);

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
    @CardUsed(RayOfCommand.class)
    @DisplayName("When controlled by another player, it is put on its owner's library")
    void putsOnOwnersLibraryWhenControlledByAnotherPlayer() {
        Card ownerFiller = new PhyrexianPurge();
        Card controllerFiller = new PhyrexianPurge();
        harness.setLibrary(player1, List.of(ownerFiller));
        harness.setLibrary(player2, List.of(controllerFiller));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, zombie.getId());

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        destroyWithPhyrexianPurge(player2, zombie);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), ownerFiller);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerFiller);
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player2, "Gravebane Zombie");
    }

    private void destroyWithPhyrexianPurge(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new PhyrexianPurge()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(caster);
        harness.castAndResolveSorcery(caster, 0, List.of(target.getId()));
    }
}
