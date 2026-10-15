package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.n.NyxbornRollicker;
import com.github.laxika.magicalvibes.cards.x.XenagosGodOfRevels;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatedRetribution.class, GrizzlyBears.class, JaceBeleren.class, FountainOfYouth.class,
        NyxbornRollicker.class, XenagosGodOfRevels.class})
class FatedRetributionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and planeswalkers but not other permanents")
    void destroysCreaturesAndPlaneswalkers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        cast(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(fountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Scry 2 is available when cast on your turn")
    void scriesOnYourTurn() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        cast(player1);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Does not scry when cast on an opponent's turn")
    void doesNotScryOnOpponentsTurn() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        cast(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Destroys creatures and planeswalkers on an opponent's turn without scrying")
    void destroysPermanentsOnOpponentsTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setLibrary(player1, List.of(new FatedRetribution(), new FatedRetribution()));

        cast(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.assertInGraveyard(player1, "Fated Retribution");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Destruction happens before scry, which can put either revealed card on the bottom")
    void destroysBeforeScryAndOrdersLibrary() {
        harness.addToBattlefield(player2, new NyxbornRollicker());
        FatedRetribution first = new FatedRetribution();
        FatedRetribution second = new FatedRetribution();
        FatedRetribution third = new FatedRetribution();
        harness.setLibrary(player1, List.of(first, second, third));

        cast(player1);

        harness.assertInGraveyard(player2, "Nyxborn Rollicker");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Fated Retribution");
    }

    @Test
    @DisplayName("Scry 2 with one card in the library reveals only that card")
    void scriesWithOneCardInLibrary() {
        FatedRetribution onlyCard = new FatedRetribution();
        harness.setLibrary(player1, List.of(onlyCard));

        cast(player1);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent destruction or leave a scry choice pending")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new NyxbornRollicker());

        cast(player1);

        harness.assertInGraveyard(player2, "Nyxborn Rollicker");
        harness.assertInGraveyard(player1, "Fated Retribution");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An indestructible creature survives while devotion-granting creatures are destroyed")
    void indestructibleCreatureSurvives() {
        Permanent xenagos = harness.addToBattlefieldAndReturn(player2, new XenagosGodOfRevels());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new NyxbornRollicker());
        }

        cast(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(xenagos);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        harness.assertNotInGraveyard(player2, "Xenagos, God of Revels");
    }

    private void cast(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FatedRetribution(), "{4}{W}{W}{W}");
        harness.passBothPriorities();
    }
}
