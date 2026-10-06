package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneSnag.class, RonomUnicorn.class})
class RuneSnagTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay the base cost")
    void countersWhenControllerCannotPayBaseCost() {
        RonomUnicorn unicorn = new RonomUnicorn();
        harness.setHand(player1, List.of(unicorn));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unicorn.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Ronom Unicorn");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can pay the base cost")
    void controllerCanPayBaseCost() {
        RonomUnicorn unicorn = new RonomUnicorn();
        harness.setHand(player1, List.of(unicorn));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unicorn.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Ronom Unicorn")).isNotNull();
    }

    @Test
    @DisplayName("Adds two to the cost for each Rune Snag in all graveyards")
    void costScalesWithRuneSnagsInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new RuneSnag()));
        harness.setGraveyard(player2, List.of(new RuneSnag()));

        RonomUnicorn unicorn = new RonomUnicorn();
        harness.setHand(player1, List.of(unicorn));
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unicorn.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Ronom Unicorn")).isNotNull();
    }

    @Test
    @DisplayName("Ignores other cards in all graveyards when scaling the cost")
    void ignoresOtherCardsInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new RonomUnicorn()));
        harness.setGraveyard(player2, List.of(new RuneSnag()));

        RonomUnicorn unicorn = new RonomUnicorn();
        harness.setHand(player1, List.of(unicorn));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unicorn.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Ronom Unicorn")).isNotNull();
    }

    @Test
    @DisplayName("Counters when the controller can pay the base cost but not the scaled cost")
    void countersWhenControllerCannotPayScaledCost() {
        harness.setGraveyard(player1, List.of(new RuneSnag()));
        harness.setGraveyard(player2, List.of(new RuneSnag()));
        RonomUnicorn unicorn = new RonomUnicorn();
        harness.setHand(player1, List.of(unicorn));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unicorn.getId());

        harness.assertInGraveyard(player1, "Ronom Unicorn");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target a spell controlled by its own controller")
    void canCounterOwnSpell() {
        RonomUnicorn unicorn = new RonomUnicorn();
        harness.setHand(player1, List.of(unicorn, new RuneSnag()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, unicorn.getId());

        harness.assertInGraveyard(player1, "Ronom Unicorn");
        harness.assertInGraveyard(player1, "Rune Snag");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters the spell when its controller declines to pay")
    void countersWhenControllerDeclinesToPay() {
        RonomUnicorn unicorn = new RonomUnicorn();
        harness.setHand(player1, List.of(unicorn));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unicorn.getId());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ronom Unicorn");
    }
}
