package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightTilling.class, Forest.class, Shock.class, MistmeadowCouncil.class})
class MidnightTillingTest extends BaseCardTest {

    private void castAndResolveToMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MidnightTilling()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("Mills four cards then prompts to return a milled permanent")
    void millsThenMayPrompt() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Shock(), new Shock(), new Shock()));

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may returns a milled permanent to hand")
    void acceptingMayReturnsMilledPermanent() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Shock(), new Shock(), new Shock()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining may leaves milled cards in the graveyard")
    void decliningMayLeavesMilledCards() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Shock(), new Shock(), new Shock()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not offer a may when no permanent was milled")
    void noPermanentMilled() {
        harness.setLibrary(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock()));

        castAndResolveToMay();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Returns only one of multiple milled permanents")
    void returnsOnlyOnePermanent() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second, new MidnightTilling(), new MidnightTilling()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can decline the first permanent and return a later one")
    void canReturnLaterPermanent() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second, new MidnightTilling(), new MidnightTilling()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mills only four cards and does not offer older graveyard cards")
    void onlyMilledCardsAreEligible() {
        Forest older = new Forest();
        Forest fifth = new Forest();
        harness.setGraveyard(player1, List.of(older));
        harness.setLibrary(player1, List.of(new MidnightTilling(), new MidnightTilling(),
                new MidnightTilling(), new MidnightTilling(), fifth));

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6).contains(older);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A short library still allows returning a milled permanent")
    void shortLibraryStillReturnsPermanent() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new MidnightTilling()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library offers no return and does not lose the game")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());

        castAndResolveToMay();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can return a nonland permanent without putting it onto the battlefield")
    void returnsCreatureToHandWithoutEnteringBattlefield() {
        MistmeadowCouncil creature = new MistmeadowCouncil();
        MidnightTilling fifth = new MidnightTilling();
        harness.setLibrary(player1, List.of(creature, new MidnightTilling(),
                new MidnightTilling(), new MidnightTilling(), fifth));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can decline all eligible permanents")
    void canDeclineAllPermanents() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second, new MidnightTilling(), new MidnightTilling()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5).contains(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
