package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({WhipSpineDrake.class, BlindPhantasm.class})
class WhipSpineDrakeTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        Permanent drake = castFaceDown();
        assertThat(drake.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int drakeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drake);
        harness.turnFaceUp(player1, drakeIndex);
        harness.passBothPriorities();

        assertThat(drake.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Whip-Spine Drake")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new WhipSpineDrake());
        addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Requires white mana to turn face up for its morph cost")
    void requiresWhiteManaToTurnFaceUp() {
        Permanent drake = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(drake)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(drake.isFaceDown()).isTrue();
    }

    @Test
    void faceDownDrakeCanBeBlockedByNonFlyingCreature() {
        Permanent drake = castFaceDown();
        drake.setSummoningSick(false);
        addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0)))).doesNotThrowAnyException();
    }

    @Test
    void turningFaceUpRestoresFlyingImmediatelyWithoutUsingStack() {
        Permanent drake = castFaceDown();
        drake.setSummoningSick(false);
        addCreatureReady(player2, new BlindPhantasm());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.turnFaceUp(player1, 0);

        assertThat(drake.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void requiresTwoGenericManaInAdditionToWhiteToTurnFaceUp() {
        Permanent drake = castFaceDown();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(drake.isFaceDown()).isTrue();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new WhipSpineDrake()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Whip-Spine Drake");
    }
}
