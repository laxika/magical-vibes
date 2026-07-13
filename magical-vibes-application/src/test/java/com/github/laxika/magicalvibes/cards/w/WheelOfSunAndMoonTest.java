package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WheelOfSunAndMoonTest extends BaseCardTest {

    // ===== Casting / attachment =====

    @Test
    @DisplayName("Resolving Wheel of Sun and Moon attaches it to the target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new WheelOfSunAndMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wheel of Sun and Moon")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    // ===== Replacement: milled =====

    @Test
    @DisplayName("Enchanted player's milled card is put on the bottom of their library, not the graveyard")
    void milledCardGoesToBottomOfLibrary() {
        placeWheelOnPlayer(player2, player2); // controlled by and enchanting player2
        addReadyMillstone(player1);            // player1's only permanent → index 0
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears()); // top
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears()); // bottom

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Nothing hit the graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // The milled card is now on the bottom of the library
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(2);
        assertThat(deck.get(deck.size() - 1).getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Replacement: creature dies =====

    @Test
    @DisplayName("Enchanted player's creature is put on the bottom of their library instead of dying")
    void dyingCreatureGoesToBottomOfLibrary() {
        placeWheelOnPlayer(player1, player2);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Only the enchanted player is affected =====

    @Test
    @DisplayName("A non-enchanted player's milled cards still go to the graveyard")
    void nonEnchantedPlayerUnaffected() {
        placeWheelOnPlayer(player2, player2); // Wheel enchants player2, not player1
        addReadyMillstone(player1);           // player1's only permanent → index 0
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        // Wheel enchants player2, so player1's milled card hits the graveyard normally
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Helpers =====

    private Permanent placeWheelOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent wheel = new Permanent(new WheelOfSunAndMoon());
        wheel.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(wheel);
        return wheel;
    }

    private Permanent addReadyMillstone(Player player) {
        Permanent perm = new Permanent(new Millstone());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
