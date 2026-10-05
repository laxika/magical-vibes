package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KrumarBondKin.class)
class KrumarBondKinTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new KrumarBondKin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent krumar = findPermanent(player1, "Krumar Bond-Kin");
        assertThat(krumar.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int krumarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(krumar);
        harness.turnFaceUp(player1, krumarIndex);
        harness.passBothPriorities();

        assertThat(krumar.isFaceDown()).isFalse();
    }

    @Test
    void cannotCastFaceDownWithoutThreeMana() {
        harness.setHand(player1, List.of(new KrumarBondKin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpWithoutBlackMana() {
        Permanent krumar = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(krumar.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpWithOnlyFourMana() {
        Permanent krumar = castFaceDown();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(krumar.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpIsImmediateAndDoesNotTapTheCreature() {
        Permanent krumar = castFaceDown();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.turnFaceUp(player1, 0);

        assertThat(krumar.isFaceDown()).isFalse();
        assertThat(krumar.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(krumar);
    }

    @Test
    void canBeCastNormallyWithoutTurningFaceUp() {
        harness.castFromHand(player1, new KrumarBondKin(), "{3}{B}{B}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Krumar Bond-Kin").isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new KrumarBondKin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Krumar Bond-Kin");
    }
}
