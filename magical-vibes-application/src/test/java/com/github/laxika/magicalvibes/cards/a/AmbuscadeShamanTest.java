package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AmbuscadeShaman.class, GrizzlyBears.class, FugitiveWizard.class})
class AmbuscadeShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gives it +2/+2 until end of turn")
    void ownEntryBoostsItself() {
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        addManaForNormalCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent shaman = findPermanent(player1, "Ambuscade Shaman");
        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another creature entering under its control gets +2/+2 until end of turn")
    void anotherAllyEntryBoostsThatCreature() {
        harness.addToBattlefield(player1, new AmbuscadeShaman());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("It does not trigger for an opponent's creature")
    void opponentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new AmbuscadeShaman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player2, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AmbuscadeShaman());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dash grants haste and returns it to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new AmbuscadeShaman()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent shaman = findPermanent(player1, "Ambuscade Shaman");
        assertThat(shaman.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ambuscade Shaman");
        harness.assertNotOnBattlefield(player1, "Ambuscade Shaman");
    }

    private void addManaForNormalCast() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
