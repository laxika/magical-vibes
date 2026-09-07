package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuffocatingFumes.class, FugitiveWizard.class, GrizzlyBears.class})
class SuffocatingFumesTest extends BaseCardTest {

    private void castFumes() {
        harness.setHand(player1, List.of(new SuffocatingFumes()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Shrinks only creatures controlled by opponents")
    void shrinksOpponentsCreaturesOnly() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFumes();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-1/-1 sends an opponent's 1/1 to its graveyard")
    void killsOpponentOneOne() {
        harness.addToBattlefield(player2, new FugitiveWizard());

        castFumes();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("The shrink wears off at end of turn")
    void wearsOff() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFumes();
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling {2} discards Suffocating Fumes and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new SuffocatingFumes()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Suffocating Fumes");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
