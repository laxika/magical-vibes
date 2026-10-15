package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoopedUp;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TanglespanLookout.class, Pacifism.class, GloriousAnthem.class, GrizzlyBears.class, CoopedUp.class})
class TanglespanLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an Aura you control enters")
    void drawsWhenControlledAuraEnters() {
        harness.addToBattlefield(player1, new TanglespanLookout());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism(), new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger for a non-Aura enchantment")
    void doesNotDrawForNonAuraEnchantment() {
        harness.addToBattlefield(player1, new TanglespanLookout());
        harness.setHand(player1, List.of(new GloriousAnthem(), new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for an Aura controlled by an opponent")
    void doesNotDrawForOpponentsAura() {
        harness.addToBattlefield(player1, new TanglespanLookout());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Aura entry triggers, and the draw waits for the trigger to resolve")
    void drawsForEachAuraEnchantingOwnCreature() {
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new TanglespanLookout());
        TanglespanLookout firstDraw = new TanglespanLookout();
        TanglespanLookout secondDraw = new TanglespanLookout();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new CoopedUp(), new CoopedUp()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, lookout.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw).hasSize(2);

        harness.castEnchantment(player1, 0, lookout.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two Lookouts each draw a card for the same Aura entry")
    void eachLookoutTriggersIndependently() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TanglespanLookout());
        harness.addToBattlefield(player1, new TanglespanLookout());
        TanglespanLookout firstDraw = new TanglespanLookout();
        TanglespanLookout secondDraw = new TanglespanLookout();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new CoopedUp()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw resolves after both the Lookout and triggering Aura leave")
    void drawSurvivesSourceAndAuraLeaving() {
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new TanglespanLookout());
        TanglespanLookout drawnCard = new TanglespanLookout();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new CoopedUp()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, lookout.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Tanglespan Lookout");
        harness.assertInGraveyard(player1, "Cooped Up");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the Aura controller's Lookout draws when both players have a Lookout")
    void drawsOnlyForAuraControllerWithOpposingLookouts() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TanglespanLookout());
        harness.addToBattlefield(player2, new TanglespanLookout());
        TanglespanLookout drawnCard = new TanglespanLookout();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new CoopedUp()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature entering without being cast does not trigger the Lookout")
    void doesNotDrawForCreatureEnteringWithoutBeingCast() {
        harness.addToBattlefield(player1, new TanglespanLookout());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new TanglespanLookout());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
