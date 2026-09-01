package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaliciousEclipse.class, GrizzlyBears.class, SerraAngel.class, Shock.class})
class MaliciousEclipseTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2, but only exiles opponent creatures that die")
    void shrinksAllCreaturesAndOnlyExilesOpponents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castEclipse();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles an opponent creature that dies later in the same turn")
    void replacementAppliesToLaterDeathsThisTurn() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castEclipse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Serra Angel");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("The -2/-2 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castEclipse();

        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    private void castEclipse() {
        harness.setHand(player1, List.of(new MaliciousEclipse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
