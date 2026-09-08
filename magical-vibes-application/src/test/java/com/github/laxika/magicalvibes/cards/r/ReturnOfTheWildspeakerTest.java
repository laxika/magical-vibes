package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StaunchDefenders;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnOfTheWildspeaker.class, GrizzlyBears.class, StaunchDefenders.class})
class ReturnOfTheWildspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Draw mode counts the greatest power among your non-Human creatures")
    void drawsGreatestNonHumanPower() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new StaunchDefenders());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        cast(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Pump mode affects only your non-Human creatures and wears off at end of turn")
    void pumpsOnlyYourNonHumanCreaturesUntilEndOfTurn() {
        Permanent ownNonHuman = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownHuman = addCreatureReady(player1, new StaunchDefenders());
        Permanent opponentNonHuman = addCreatureReady(player2, new GrizzlyBears());

        cast(player1, 1);

        assertThat(ownNonHuman.getEffectivePower()).isEqualTo(5);
        assertThat(ownNonHuman.getEffectiveToughness()).isEqualTo(5);
        assertThat(ownHuman.getEffectivePower()).isEqualTo(3);
        assertThat(ownHuman.getEffectiveToughness()).isEqualTo(4);
        assertThat(opponentNonHuman.getEffectivePower()).isEqualTo(2);
        assertThat(opponentNonHuman.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownNonHuman.getEffectivePower()).isEqualTo(2);
        assertThat(ownNonHuman.getEffectiveToughness()).isEqualTo(2);
    }

    private void cast(Player player, int mode) {
        harness.setHand(player, List.of(new ReturnOfTheWildspeaker()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.castModalInstant(player, 0, mode, List.of());
        harness.passBothPriorities();
    }
}
