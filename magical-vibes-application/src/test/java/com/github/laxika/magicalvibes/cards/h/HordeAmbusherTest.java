package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CanyonLurkers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HordeAmbusherTest extends BaseCardTest {

    @Test
    void morphRequiresAndRevealsARedCard() {
        CanyonLurkers redCard = new CanyonLurkers();
        harness.setHand(player1, List.of(new HordeAmbusher(), redCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0, 1);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Horde Ambusher").isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(redCard);
    }

    @Test
    void turningFaceUpMakesTargetCreatureUnableToBlockThisTurn() {
        CanyonLurkers redCard = new CanyonLurkers();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HordeAmbusher(), redCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0, 1);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ambusher = findPermanent(player1, "Horde Ambusher");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ambusher), 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(blocker.getId());
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blockingDealsOneDamageToItsController() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ambusher = addCreatureReady(player2, new HordeAmbusher());
        harness.setLife(player2, 20);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ambusher.isBlocking()).isTrue();
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }
}
