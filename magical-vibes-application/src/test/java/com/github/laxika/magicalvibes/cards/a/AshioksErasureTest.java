package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshioksErasure.class, GrizzlyBears.class, SternDismissal.class})
class AshioksErasureTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the targeted spell")
    void etbExilesTargetSpell() {
        GrizzlyBears bears = castErasureOnSpell();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Opponents cannot cast spells with the exiled card's name")
    void opponentsCannotCastSpellsWithExiledName() {
        castErasureOnSpell();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The controller can cast a spell with the exiled card's name")
    void controllerCanCastSpellsWithExiledName() {
        castErasureOnSpell();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The exiled spell returns to its owner's hand when Erasure leaves")
    void exiledSpellReturnsWhenErasureLeaves() {
        GrizzlyBears bears = castErasureOnSpell();
        Permanent erasure = findPermanent(player1, "Ashiok's Erasure");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, erasure));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaving before the enter trigger resolves still exiles the spell permanently")
    void leavingBeforeEnterTriggerResolvesStillExilesSpell() {
        GrizzlyBears bears = castErasureWithPendingEnterTrigger();
        Permanent erasure = findPermanent(player1, "Ashiok's Erasure");
        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, erasure.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Ashiok's Erasure");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponents can cast the returned spell after Erasure leaves")
    void restrictionEndsWhenErasureLeaves() {
        GrizzlyBears bears = castErasureOnSpell();
        Permanent erasure = findPermanent(player1, "Ashiok's Erasure");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, erasure));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell controlled by another player returns to its actual owner's hand")
    void returnsSpellToOwnerRatherThanController() {
        GrizzlyBears bears = castErasureWithPendingEnterTrigger();
        harness.inMutationScope(() -> gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(bears.getId()))
                .findFirst().orElseThrow().setOwnerIdOverride(player1.getId()));
        harness.passBothPriorities();
        Permanent erasure = findPermanent(player1, "Ashiok's Erasure");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, erasure));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
    private GrizzlyBears castErasureOnSpell() {
        GrizzlyBears bears = castErasureWithPendingEnterTrigger();
        harness.passBothPriorities();
        return bears;
    }

    private GrizzlyBears castErasureWithPendingEnterTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);
        harness.castFromHand(player1, new AshioksErasure(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }
}
