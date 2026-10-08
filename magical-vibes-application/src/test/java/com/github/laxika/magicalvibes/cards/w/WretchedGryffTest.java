package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Convolute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WretchedGryff.class, GrizzlyBears.class, Convolute.class})
class WretchedGryffTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, controller draws a card")
    void hardcastDrawsACard() {
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve cast trigger
        harness.assertInHand(player1, "Grizzly Bears");

        harness.passBothPriorities(); // resolve creature spell
        harness.assertOnBattlefield(player1, "Wretched Gryff");
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value, draw")
    void emergeSacrificesReducesCostAndDraws() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        // Emerge {5}{U} reduced by 2 → {3}{U}
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Wretched Gryff");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Emerge fails without enough mana after reduction")
    void emergeFailsWithInsufficientMana() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new WretchedGryff()));
        // Need {3}{U} after reduction; only {2}{U} available
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emergeWithHighManaValueCreatureStillCostsOneBlue() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new WretchedGryff()).getId();
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.setLibrary(player1, List.of(new WretchedGryff()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId));

        harness.assertInGraveyard(player1, "Wretched Gryff");
        harness.assertNotOnBattlefield(player1, "Wretched Gryff");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Wretched Gryff");
        harness.assertNotOnBattlefield(player1, "Wretched Gryff");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wretched Gryff");
    }

    @Test
    void emergeCannotReduceTheBlueManaRequirement() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new WretchedGryff()).getId();
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wretched Gryff");
        harness.assertInHand(player1, "Wretched Gryff");
        harness.assertNotInGraveyard(player1, "Wretched Gryff");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emergeCannotSacrificeAnOpponentsCreature() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player2, new WretchedGryff()).getId();
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Wretched Gryff");
        harness.assertInHand(player1, "Wretched Gryff");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emergeRequiresACreatureSacrifice() {
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Wretched Gryff");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDoesNotDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new WretchedGryff()));

        harness.enterBattlefieldAndReturn(player1, new WretchedGryff());

        harness.assertOnBattlefield(player1, "Wretched Gryff");
        harness.assertNotInHand(player1, "Wretched Gryff");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawTriggerResolvesEvenIfCreatureSpellIsCountered() {
        WretchedGryff gryff = new WretchedGryff();
        harness.setHand(player1, List.of(gryff));
        harness.setLibrary(player1, List.of(new WretchedGryff()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setHand(player2, List.of(new Convolute()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, gryff.getId());

        harness.assertInGraveyard(player1, "Wretched Gryff");
        harness.assertNotOnBattlefield(player1, "Wretched Gryff");
        harness.assertNotInHand(player1, "Wretched Gryff");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Wretched Gryff");
        harness.assertNotOnBattlefield(player1, "Wretched Gryff");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new WretchedGryff());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockGryff() {
        addCreatureReady(player1, new WretchedGryff());
        var blocker = addCreatureReady(player2, new WretchedGryff());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
