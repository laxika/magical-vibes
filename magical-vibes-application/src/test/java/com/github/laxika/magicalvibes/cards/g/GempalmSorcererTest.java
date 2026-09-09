package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GempalmSorcerer.class, FugitiveWizard.class, GrizzlyBears.class})
class GempalmSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling gives Wizard creatures flying")
    void cyclingGivesWizardsFlying() {
        Permanent ownWizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponentWizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Permanent nonWizard = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GempalmSorcerer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownWizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentWizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonWizard, Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Gempalm Sorcerer");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling's flying grant wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new GempalmSorcerer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isFalse();
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
