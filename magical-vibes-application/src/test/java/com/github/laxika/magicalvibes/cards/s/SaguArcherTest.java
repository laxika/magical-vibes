package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlabasterKirin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaguArcher.class, AlabasterKirin.class})
class SaguArcherTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new SaguArcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent archer = findPermanent(player1, "Sagu Archer");
        assertThat(archer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int archerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(archer);
        harness.turnFaceUp(player1, archerIndex);

        assertThat(archer.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AlabasterKirin());
        Permanent archer = addCreatureReady(player2, new SaguArcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archer.isBlocking()).isTrue();
    }

    @Test
    void faceDownArcherCannotBlockFlyingCreature() {
        harness.setHand(player1, List.of(new SaguArcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent archer = findPermanent(player1, "Sagu Archer");
        addCreatureReady(player2, new AlabasterKirin());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(archer.isFaceDown()).isTrue();
        assertThat(archer.isBlocking()).isFalse();
    }

    @Test
    void cannotTurnFaceUpWithoutGreenMana() {
        harness.setHand(player1, List.of(new SaguArcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent archer = findPermanent(player1, "Sagu Archer");
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(archer.isFaceDown()).isTrue();
    }
}
