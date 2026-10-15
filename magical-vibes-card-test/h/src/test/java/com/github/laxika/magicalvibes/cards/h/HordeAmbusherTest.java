package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CanyonLurkers;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HordeAmbusher.class, CanyonLurkers.class, WetlandSambar.class})
class HordeAmbusherTest extends BaseCardTest {

    @Test
    void castingFaceDownDoesNotRequireARedCard() {
        harness.setHand(player1, List.of(new HordeAmbusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Horde Ambusher").isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void turningFaceUpMakesTargetCreatureUnableToBlockThisTurn() {
        CanyonLurkers redCard = new CanyonLurkers();
        Permanent attacker = addCreatureReady(player1, new WetlandSambar());
        Permanent blocker = addCreatureReady(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new HordeAmbusher(), redCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ambusher = findPermanent(player1, "Horde Ambusher");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ambusher), 0);
        assertThat(ambusher.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(redCard);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(blocker.getId());
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blockingDealsOneDamageToItsController() {
        Permanent attacker = addCreatureReady(player1, new WetlandSambar());
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

    @Test
    void revealingARedCardTurnsFaceUpWithoutManaAndCanTargetItself() {
        Permanent ambusher = addCreatureReady(player1, new HordeAmbusher());
        ambusher.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        CanyonLurkers redCard = new CanyonLurkers();
        harness.setHand(player1, List.of(redCard));

        harness.turnFaceUp(player1, 0, 0);

        assertThat(ambusher.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(redCard);
        harness.handlePermanentChosen(player1, ambusher.getId());
        harness.passBothPriorities();
        assertThat(ambusher.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void manaCannotReplaceRevealingARedCard() {
        Permanent ambusher = addCreatureReady(player1, new HordeAmbusher());
        ambusher.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new WetlandSambar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ambusher.isFaceDown()).isTrue();
    }

    @Test
    void faceDownBlockingDoesNotDealDamageToItsController() {
        Permanent attacker = addCreatureReady(player1, new WetlandSambar());
        Permanent ambusher = addCreatureReady(player2, new HordeAmbusher());
        ambusher.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setLife(player2, 20);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
