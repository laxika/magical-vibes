package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrumarBondKin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessRipper.class, KrumarBondKin.class, GrizzlyBears.class})
class RuthlessRipperTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndTurnsFaceUpByRevealingABlackCard() {
        KrumarBondKin blackCard = new KrumarBondKin();
        harness.setHand(player1, List.of(new RuthlessRipper(), blackCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ripper = findPermanent(player1, "Ruthless Ripper");
        harness.setLife(player2, 20);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ripper), 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(ripper.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard);
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotTurnFaceUpWithoutRevealingABlackCard() {
        harness.setHand(player1, List.of(new RuthlessRipper(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ripper = findPermanent(player1, "Ruthless Ripper");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ripper), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Revealed card must be black card");
        assertThat(ripper.isFaceDown()).isTrue();
    }

    @Test
    void faceUpTriggerCanTargetItsControllerAndUsesTheStack() {
        KrumarBondKin blackCard = new KrumarBondKin();
        harness.setHand(player1, List.of(new RuthlessRipper(), blackCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.turnFaceUp(player1, 0, 0);
        assertThat(findPermanent(player1, "Ruthless Ripper").isFaceDown()).isFalse();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.assertLife(player1, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard);
    }

    @Test
    void cannotTurnFaceUpWithAnEmptyHand() {
        harness.setHand(player1, List.of(new RuthlessRipper()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must reveal");
        assertThat(findPermanent(player1, "Ruthless Ripper").isFaceDown()).isTrue();
    }

    @Test
    void castingFaceUpDoesNotTriggerLifeLoss() {
        harness.setHand(player1, List.of(new RuthlessRipper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ruthless Ripper");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpBeforeCombatDamageGivesDeathtouch() {
        harness.setHand(player1, List.of(new RuthlessRipper(), new KrumarBondKin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        findPermanent(player1, "Ruthless Ripper").setSummoningSick(false);
        addCreatureReady(player2, new KrumarBondKin());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.turnFaceUp(player1, 0, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        resolveCombat();

        harness.assertInGraveyard(player1, "Ruthless Ripper");
        harness.assertInGraveyard(player2, "Krumar Bond-Kin");
        harness.assertLife(player2, 18);
    }

    @Test
    void faceDownRipperDoesNotHaveDeathtouch() {
        harness.setHand(player1, List.of(new RuthlessRipper()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        findPermanent(player1, "Ruthless Ripper").setSummoningSick(false);
        addCreatureReady(player2, new KrumarBondKin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Ruthless Ripper");
        harness.assertOnBattlefield(player2, "Krumar Bond-Kin");
        harness.assertNotInGraveyard(player2, "Krumar Bond-Kin");
    }
}
