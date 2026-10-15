package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IsperiaSupremeJudge;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuildFeud.class, Forest.class, HillGiant.class, LlanowarElves.class, Shock.class,
        DrudgeBeetle.class, IsperiaSupremeJudge.class})
class GuildFeudTest extends BaseCardTest {

    /** Resolves Guild Feud's upkeep trigger targeting player2, leaving the first reveal pending. */
    private void triggerAgainstPlayer2() {
        harness.addToBattlefield(player1, new GuildFeud());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    private void answerReveal(Player player, Card chosen) {
        harness.handleMultipleCardsChosen(player, chosen == null ? List.<UUID>of() : List.of(chosen.getId()));
    }

    @Test
    @DisplayName("Upkeep trigger only offers opponents as targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new GuildFeud());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Opponent reveals first and is only offered the revealed creature cards")
    void opponentRevealsFirstAndPicksAmongCreatures() {
        HillGiant giant = new HillGiant();
        harness.setLibrary(player2, List.of(giant, new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Shock(), new Forest()));

        triggerAgainstPlayer2();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(giant.getId());
    }

    @Test
    @DisplayName("Both creatures enter and fight each other")
    void bothCreaturesEnterAndFight() {
        HillGiant giant = new HillGiant();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player2, List.of(giant, new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(elves, new Shock(), new Forest()));

        triggerAgainstPlayer2();
        answerReveal(player2, giant);
        answerReveal(player1, elves);

        // Hill Giant (3/3) kills Llanowar Elves (1/1) and survives with 1 damage marked.
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Hill Giant"))
                .singleElement()
                .matches(permanent -> permanent.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Unchosen revealed cards go to their owner's graveyard")
    void remainingRevealedCardsGoToGraveyard() {
        HillGiant giant = new HillGiant();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player2, List.of(giant, new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(elves, new Shock(), new Forest()));

        triggerAgainstPlayer2();
        answerReveal(player2, giant);
        answerReveal(player1, elves);

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Shock", "Forest");
    }

    @Test
    @DisplayName("No fight happens when the opponent declines to put a creature onto the battlefield")
    void decliningTheFirstCreatureSkipsTheFight() {
        HillGiant giant = new HillGiant();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player2, List.of(giant, new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(elves, new Shock(), new Forest()));

        triggerAgainstPlayer2();
        answerReveal(player2, null);
        answerReveal(player1, elves);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Llanowar Elves"))
                .singleElement()
                .matches(permanent -> permanent.getMarkedDamage() == 0);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A player with no revealed creature is not prompted and mills the three cards")
    void noCreatureRevealedSkipsThePrompt() {
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Forest()));
        harness.setLibrary(player1, List.of(elves, new Shock(), new Forest()));

        triggerAgainstPlayer2();

        // player2 had nothing to choose, so the pending choice is already player1's.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);

        answerReveal(player1, elves);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void decliningTheSecondCreatureSkipsTheFight() {
        DrudgeBeetle opponentCreature = new DrudgeBeetle();
        DrudgeBeetle controllerCreature = new DrudgeBeetle();
        harness.setLibrary(player2, List.of(opponentCreature));
        harness.setLibrary(player1, List.of(controllerCreature));

        triggerAgainstPlayer2();
        answerReveal(player2, opponentCreature);
        answerReveal(player1, null);

        harness.assertOnBattlefield(player2, "Drudge Beetle");
        harness.assertInGraveyard(player1, "Drudge Beetle");
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .matches(permanent -> permanent.getMarkedDamage() == 0);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void equalCreaturesFromShortLibrariesBothDieInTheFight() {
        DrudgeBeetle opponentCreature = new DrudgeBeetle();
        DrudgeBeetle controllerCreature = new DrudgeBeetle();
        harness.setLibrary(player2, List.of(opponentCreature, new Forest()));
        harness.setLibrary(player1, List.of(controllerCreature));

        triggerAgainstPlayer2();
        answerReveal(player2, opponentCreature);
        answerReveal(player1, controllerCreature);

        harness.assertNotOnBattlefield(player1, "Drudge Beetle");
        harness.assertNotOnBattlefield(player2, "Drudge Beetle");
        harness.assertInGraveyard(player1, "Drudge Beetle");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Drudge Beetle", "Forest");
    }

    @Test
    void cardsBelowTheTopThreeAreNotRevealedOrMilled() {
        DrudgeBeetle fourthCard = new DrudgeBeetle();
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), fourthCard));
        harness.setLibrary(player1, List.of());

        triggerAgainstPlayer2();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourthCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player2, "Drudge Beetle");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyOpponentLibraryStillAllowsControllerToPutInACreature() {
        DrudgeBeetle creature = new DrudgeBeetle();
        harness.setLibrary(player2, List.of());
        harness.setLibrary(player1, List.of(creature));

        triggerAgainstPlayer2();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId())
                .isEqualTo(player1.getId());
        answerReveal(player1, creature);

        harness.assertOnBattlefield(player1, "Drudge Beetle");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void legendRuleWaitsUntilAfterBothRevealsAndTheFight() {
        harness.addToBattlefield(player2, new IsperiaSupremeJudge());
        IsperiaSupremeJudge revealedIsperia = new IsperiaSupremeJudge();
        DrudgeBeetle beetle = new DrudgeBeetle();
        harness.setLibrary(player2, List.of(revealedIsperia));
        harness.setLibrary(player1, List.of(beetle));

        triggerAgainstPlayer2();
        answerReveal(player2, revealedIsperia);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);

        answerReveal(player1, beetle);

        harness.assertInGraveyard(player1, "Drudge Beetle");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(revealedIsperia.getId()))
                .singleElement().matches(permanent -> permanent.getMarkedDamage() == 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }
}
