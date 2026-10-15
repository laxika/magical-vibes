package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighspireBellRinger.class, GrizzlyBears.class})
class HighspireBellRingerTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell each turn does not get the reduction")
    void firstSpellIsNotReduced() {
        harness.addToBattlefield(player1, new HighspireBellRinger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the second spell each turn costs {1} less")
    void onlySecondSpellIsReduced() {
        harness.addToBattlefield(player1, new HighspireBellRinger());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to an opponent's spells")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new HighspireBellRinger());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Bell-Ringer cast as the first spell reduces the next spell")
    void countsItsOwnCastBeforeEntering() {
        harness.setHand(player1, List.of(new HighspireBellRinger(), new HighspireBellRinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Highspire Bell-Ringer")).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Bell-Ringers each reduce the second spell")
    void reductionsStack() {
        harness.addToBattlefield(player1, new HighspireBellRinger());
        harness.addToBattlefield(player1, new HighspireBellRinger());
        harness.setHand(player1, List.of(new HighspireBellRinger(), new HighspireBellRinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Highspire Bell-Ringer")).isEqualTo(4);
    }

    @Test
    @DisplayName("Excess generic reduction cannot pay the colored mana requirement")
    void reductionDoesNotRemoveColoredMana() {
        harness.addToBattlefield(player1, new HighspireBellRinger());
        harness.addToBattlefield(player1, new HighspireBellRinger());
        harness.setHand(player1, List.of(new HighspireBellRinger(), new HighspireBellRinger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
