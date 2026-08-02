package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterOfPredicamentsTest extends BaseCardTest {

    @Test
    void wrongGuessOffersControllerFreeCastOfChosenNonlandCard() {
        Card bears = new GrizzlyBears();
        addMasterWithHand(bears);

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 4");

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void correctGuessLeavesChosenCardInHand() {
        Card bears = new GrizzlyBears();
        addMasterWithHand(bears);

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "4 or less");

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
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
        Card bears = new GrizzlyBears();
        addMasterWithHand(bears);

        resolveCombat();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, "Greater than 4");
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("Grizzly Bears"));
    }

    private Permanent addMasterWithHand(Card chosenCard) {
        Permanent master = addCreatureReady(player1, new MasterOfPredicaments());
        master.setAttacking(true);
        harness.setHand(player1, List.of(chosenCard));
        return master;
    }
}
