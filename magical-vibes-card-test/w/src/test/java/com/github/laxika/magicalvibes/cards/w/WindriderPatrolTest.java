package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CloudManta;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindriderPatrol.class, CloudManta.class})
class WindriderPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player triggers scry 2")
    void combatDamageToPlayerScriesTwo() {
        CloudManta first = new CloudManta();
        CloudManta second = new CloudManta();
        harness.setLibrary(player1, List.of(first, second));

        Permanent patrol = addCreatureReady(player1, new WindriderPatrol());
        patrol.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("Combat damage to a creature does not trigger scry")
    void combatDamageToCreatureDoesNotScry() {
        CloudManta libraryCard = new CloudManta();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent patrol = addCreatureReady(player1, new WindriderPatrol());
        patrol.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CloudManta());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Scry can keep one card and put the other below the rest of the library")
    void scrySplitsTopAndBottom() {
        CloudManta first = new CloudManta();
        CloudManta second = new CloudManta();
        CloudManta third = new CloudManta();
        harness.setLibrary(player1, List.of(first, second, third));
        addCreatureReady(player1, new WindriderPatrol()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("Scry can put both cards on the bottom in either order")
    void scryBottomsBothInChosenOrder() {
        CloudManta first = new CloudManta();
        CloudManta second = new CloudManta();
        CloudManta third = new CloudManta();
        harness.setLibrary(player1, List.of(first, second, third));
        addCreatureReady(player1, new WindriderPatrol()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    @DisplayName("Scry 2 with a one-card library looks at only that card")
    void scryWithOneCard() {
        CloudManta onlyCard = new CloudManta();
        harness.setLibrary(player1, List.of(onlyCard));
        addCreatureReady(player1, new WindriderPatrol()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("Scry with an empty library completes without requesting a choice")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new WindriderPatrol()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The attacking controller scries their own library")
    void opposingControllerScriesOwnLibrary() {
        CloudManta first = new CloudManta();
        CloudManta second = new CloudManta();
        CloudManta opponentsCard = new CloudManta();
        harness.setLibrary(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(opponentsCard));
        addCreatureReady(player2, new WindriderPatrol()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsCard);
    }
}
