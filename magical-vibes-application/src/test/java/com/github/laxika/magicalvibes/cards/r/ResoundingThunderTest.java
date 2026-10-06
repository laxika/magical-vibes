package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResoundingThunder.class, GrizzlyBears.class, SerraAngel.class})
class ResoundingThunderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3ToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals 3 damage to target creature, destroying a 2/2")
    void deals3ToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling deals 6 damage to target player and draws a card")
    void cyclingDeals6ToPlayerAndDraws() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        // The cycling draw still happens: Thunder discarded, the library card drawn.
        harness.assertInGraveyard(player1, "Resounding Thunder");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling deals 6 damage to target creature, destroying a 4/4")
    void cyclingDeals6ToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.activateHandAbility(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player1, "Resounding Thunder");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling damage resolves before the separate card draw")
    void cyclingAllowsResponsesBetweenDamageAndDraw() {
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.assertInGraveyard(player1, "Resounding Thunder");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(harness.getGameData().stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling still draws when the damage target leaves the battlefield")
    void cyclingDrawSurvivesIllegalDamageTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        harness.setHand(player2, List.of(new ResoundingThunder()));
        addCyclingMana(player1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Resounding Thunder");
    }
    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
