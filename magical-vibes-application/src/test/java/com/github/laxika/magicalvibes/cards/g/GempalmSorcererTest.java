package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.m.MacetailHystrodon;
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

@CardUsed({GempalmSorcerer.class, FugitiveWizard.class, MacetailHystrodon.class})
class GempalmSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling gives Wizard creatures flying")
    void cyclingGivesWizardsFlying() {
        Permanent ownWizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponentWizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Permanent nonWizard = harness.addToBattlefieldAndReturn(player1, new MacetailHystrodon());
        harness.setHand(player1, List.of(new GempalmSorcerer()));
        harness.setLibrary(player1, List.of(new MacetailHystrodon()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownWizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentWizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonWizard, Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Gempalm Sorcerer");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Macetail Hystrodon");
    }

    @Test
    @DisplayName("Cycling's flying grant wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new GempalmSorcerer()));
        harness.setLibrary(player1, List.of(new MacetailHystrodon()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cycling only grants flying to Wizards on the battlefield as it resolves")
    void cyclingDoesNotAffectWizardsEnteringLater() {
        harness.setHand(player1, List.of(new GempalmSorcerer()));
        harness.setLibrary(player1, List.of(new MacetailHystrodon()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent laterWizard = harness.enterBattlefieldAndReturn(player1, new FugitiveWizard());

        assertThat(gqs.hasKeyword(gd, laterWizard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wizards entering before the cycling trigger resolves gain flying")
    void wizardsEnteringBeforeResolutionGainFlying() {
        harness.setHand(player1, List.of(new GempalmSorcerer()));
        harness.setLibrary(player1, List.of(new MacetailHystrodon()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        Permanent wizard = harness.enterBattlefieldAndReturn(player2, new FugitiveWizard());
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Gempalm Sorcerer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Macetail Hystrodon");
    }

    @Test
    @DisplayName("Discarding Gempalm Sorcerer during cleanup does not grant flying")
    void ordinaryDiscardDoesNotGrantFlying() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new GempalmSorcerer(),
                new FugitiveWizard(), new FugitiveWizard(), new FugitiveWizard(),
                new FugitiveWizard(), new FugitiveWizard(), new FugitiveWizard(), new FugitiveWizard()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertInGraveyard(player1, "Gempalm Sorcerer");
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
