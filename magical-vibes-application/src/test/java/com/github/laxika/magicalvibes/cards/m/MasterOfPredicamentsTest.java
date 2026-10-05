package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.ChargingRhino;
import com.github.laxika.magicalvibes.cards.i.IllusoryAngel;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfPredicaments.class, RuneclawBear.class, Forest.class,
        ChargingRhino.class, Juggernaut.class, IllusoryAngel.class})
class MasterOfPredicamentsTest extends BaseCardTest {

    @Test
    void wrongGuessOffersControllerFreeCastOfChosenNonlandCard() {
        Card bears = new RuneclawBear();
        addMasterWithHand(bears);

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 4");

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotInHand(player1, "Runeclaw Bear");
    }

    @Test
    void correctGuessLeavesChosenCardInHand() {
        Card bears = new RuneclawBear();
        addMasterWithHand(bears);

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "4 or less");

        harness.assertInHand(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void landCannotBeCastWhenGuessIsWrong() {
        Card forest = new Forest();
        addMasterWithHand(forest);

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 4");

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningFreeCastDoesNotRevealChosenCard() {
        Card bears = new RuneclawBear();
        addMasterWithHand(bears);

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 4");
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Runeclaw Bear");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("Runeclaw Bear"));
    }

    @Test
    void wrongGuessForCardAboveFourAllowsFreeCast() {
        addMasterWithHand(new ChargingRhino());

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "4 or less");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Charging Rhino");
        harness.assertNotInHand(player1, "Charging Rhino");
    }

    @Test
    void correctGuessForCardAboveFourDoesNotRevealIt() {
        addMasterWithHand(new ChargingRhino());

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 4");

        harness.assertInHand(player1, "Charging Rhino");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("Charging Rhino"));
    }

    @Test
    void manaValueExactlyFourIsNotGreaterThanFour() {
        addMasterWithHand(new Juggernaut());

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 4");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Juggernaut");
        harness.assertNotInHand(player1, "Juggernaut");
    }

    @Test
    void emptyHandDoesNotRequireAChoice() {
        Permanent master = addCreatureReady(player1, new MasterOfPredicaments());
        master.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void freeCastStillRespectsCardSpecificCastingRestriction() {
        addMasterWithHand(new IllusoryAngel());

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 4");
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.passBothPriorities();

        harness.assertInHand(player1, "Illusory Angel");
        harness.assertNotOnBattlefield(player1, "Illusory Angel");
    }

    private void addMasterWithHand(Card chosenCard) {
        Permanent master = addCreatureReady(player1, new MasterOfPredicaments());
        master.setAttacking(true);
        harness.setHand(player1, List.of(chosenCard));
    }
}
