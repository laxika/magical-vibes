package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallOfGemstone.class, CrystalVein.class, Forest.class, Island.class, Mountain.class})
class HallOfGemstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Active player chooses a color and their lands produce it instead of their own")
    void chosenColorReplacesOwnLandMana() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "BLUE");

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The lock is symmetric — an opponent's lands produce the chosen color too")
    void chosenColorAppliesToOpponentLands() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not replace colorless mana produced by a land")
    void colorlessManaIsNotReplaced() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new CrystalVein());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The non-controller chooses during their own upkeep")
    void activePlayerChoosesEvenWhenNotTheController() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleListChoice(player2, "RED");
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The color lock wears off at end of turn")
    void lockWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mountain = findPermanent(player1, "Mountain");
        mountain.untap();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }
}
