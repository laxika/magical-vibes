package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SootfeatherFlock.class})
class SootfeatherFlockTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new SootfeatherFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        harness.passBothPriorities();

        Permanent flock = findPermanent(player1, "Sootfeather Flock");
        assertThat(flock.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(flock));
        harness.passBothPriorities();

        assertThat(flock.isFaceDown()).isFalse();
    }

    @Test
    void cannotTurnFaceUpWithOnlyColorlessMana() {
        harness.setHand(player1, List.of(new SootfeatherFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        harness.passBothPriorities();

        Permanent flock = findPermanent(player1, "Sootfeather Flock");
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(flock)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(flock.isFaceDown()).isTrue();
    }

    @Test
    void cannotCastFaceDownWithLessThanThreeMana() {
        harness.setHand(player1, List.of(new SootfeatherFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Sootfeather Flock");
        harness.assertNotOnBattlefield(player1, "Sootfeather Flock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpWithLessThanFourManaEvenWithBlackMana() {
        harness.setHand(player1, List.of(new SootfeatherFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent flock = findPermanent(player1, "Sootfeather Flock");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(flock)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(flock.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsFlyingImmediatelyWhenTurnedFaceUpDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new SootfeatherFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent flock = findPermanent(player1, "Sootfeather Flock");
        Permanent attacker = addCreatureReady(player2, new SootfeatherFlock());
        assertThat(bls.canBlockAttacker(gd, flock, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(flock));

        assertThat(flock.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(bls.canBlockAttacker(gd, flock, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }
}
