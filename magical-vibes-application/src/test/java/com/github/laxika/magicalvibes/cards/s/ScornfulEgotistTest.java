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

@CardUsed({ScornfulEgotist.class})
class ScornfulEgotistTest extends BaseCardTest {

    @Test
    void cannotCastFaceDownWithOnlyTwoMana() {
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Scornful Egotist");
        harness.assertNotOnBattlefield(player1, "Scornful Egotist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSummoningSickCreatureTurnsFaceUpImmediatelyOnOpponentsTurn() {
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        assertThat(egotist.isSummoningSick()).isTrue();
        egotist.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(egotist));

        assertThat(egotist.isFaceDown()).isFalse();
        assertThat(egotist.isTapped()).isTrue();
        assertThat(egotist.isSummoningSick()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        assertThat(egotist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        int egotistIndex = gd.playerBattlefields.get(player1.getId()).indexOf(egotist);
        harness.turnFaceUp(player1, egotistIndex);
        harness.passBothPriorities();

        assertThat(egotist.isFaceDown()).isFalse();
    }

    @Test
    void requiresBlueManaToTurnFaceUp() {
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(egotist)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(egotist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(egotist));
        harness.passBothPriorities();

        assertThat(egotist.isFaceDown()).isFalse();
    }
}
