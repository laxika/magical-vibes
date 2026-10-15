package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NickFurySpymaster.class, GrizzlyBears.class, CrawWurm.class, Forest.class, JaceBeleren.class})
class NickFurySpymasterTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking alone draws and may put a small creature onto the battlefield attacking")
    void drawsAndPutsCreatureTappedAndAttackingWithIndestructible() {
        addCreatureReady(player1, new NickFurySpymaster());
        GrizzlyBears creature = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(creature));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttackedThisTurn()).isTrue();
        assertThat(gqs.hasKeyword(gd, entered, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The hand choice excludes creatures with mana value greater than three")
    void excludesExpensiveCreatures() {
        addCreatureReady(player1, new NickFurySpymaster());
        harness.setHand(player1, List.of(new CrawWurm(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    void decliningCreatureStillDraws() {
        addCreatureReady(player1, new NickFurySpymaster());
        GrizzlyBears creature = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(creature));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, drawn);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void anotherCreatureAttackingAloneTriggersWhileNickStaysBack() {
        addCreatureReady(player1, new NickFurySpymaster());
        addCreatureReady(player1, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void twoDeclaredAttackersDoNotTrigger() {
        addCreatureReady(player1, new NickFurySpymaster());
        addCreatureReady(player1, new GrizzlyBears());
        Forest undrawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(undrawn));

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void freshlyDrawnCreatureCanEnterAndDoesNotTriggerAnotherDraw() {
        Permanent nick = addCreatureReady(player1, new NickFurySpymaster());
        GrizzlyBears drawn = new GrizzlyBears();
        Forest remaining = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, remaining));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
        });

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, entered, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nick, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringCreatureAllowsChoosingADifferentDefendingTarget() {
        addCreatureReady(player1, new NickFurySpymaster());
        harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
        });

        assertThat(gd.interaction.isAwaitingInput())
                .as("The entering creature's controller chooses between the defending player and their planeswalker")
                .isTrue();
    }
}
