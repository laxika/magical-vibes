package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DemonicGifts;
import com.github.laxika.magicalvibes.cards.d.DraugrRecruiter;
import com.github.laxika.magicalvibes.cards.f.FeedTheSerpent;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.t.ThroneOfDeath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EgonGodOfDeath.class, ThroneOfDeath.class, SnowCoveredForest.class,
        DraugrRecruiter.class, DemonicGifts.class, FeedTheSerpent.class})
class EgonGodOfDeathTest extends BaseCardTest {

    @Test
    void exilesTwoGraveyardCardsAndSurvivesUpkeep() {
        Permanent egon = harness.addToBattlefieldAndReturn(player1, new EgonGodOfDeath());
        Card first = new SnowCoveredForest();
        Card second = new DemonicGifts();
        harness.setGraveyard(player1, List.of(first, second));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(egon);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
    }

    @Test
    void withOnlyOneGraveyardCardEgonSacrificesAndDrawsWithoutExilingIt() {
        Permanent egon = harness.addToBattlefieldAndReturn(player1, new EgonGodOfDeath());
        Card graveyardCard = new SnowCoveredForest();
        Card drawCard = new DraugrRecruiter();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(drawCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(egon);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(graveyardCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void throneOfDeathMillsAndItsAbilityExilesACreatureToDraw() {
        Card milledCard = new SnowCoveredForest();
        Card creatureCard = new DraugrRecruiter();
        Card drawCard = new DemonicGifts();
        harness.setLibrary(player1, List.of(milledCard, drawCard));
        harness.setGraveyard(player1, List.of(creatureCard));
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfDeath());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard, creatureCard);

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(throne), 0, null, null);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creatureCard));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void canCastTheBackFaceForItsManaCost() {
        EgonGodOfDeath card = new EgonGodOfDeath();
        Card creatureCard = new DraugrRecruiter();
        Card drawCard = new DemonicGifts();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(creatureCard));
        harness.setLibrary(player1, List.of(drawCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void choosesExactlyTwoCardsWhenMoreAreAvailable() {
        Permanent egon = harness.addToBattlefieldAndReturn(player1, new EgonGodOfDeath());
        Card first = new SnowCoveredForest();
        Card second = new DemonicGifts();
        Card remaining = new DraugrRecruiter();
        harness.setGraveyard(player1, List.of(first, second, remaining));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(egon);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void canCastFrontFaceAndResolveItsUpkeepAbility() {
        EgonGodOfDeath card = new EgonGodOfDeath();
        Card first = new SnowCoveredForest();
        Card second = new DemonicGifts();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void throneCanMillAnEmptyLibraryWithoutLosingTheGame() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfDeath());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(throne);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void emptyGraveyardSacrificesEgonAndDraws() {
        EgonGodOfDeath card = new EgonGodOfDeath();
        Permanent egon = harness.addToBattlefieldAndReturn(player1, card);
        Card drawCard = new DemonicGifts();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(drawCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(egon);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void stillDrawsWhenEgonLeavesBeforeItsTriggerResolves() {
        Permanent egon = harness.addToBattlefieldAndReturn(player1, new EgonGodOfDeath());
        Card drawCard = new DemonicGifts();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player2, List.of(new FeedTheSerpent()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player2, 0, egon.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(egon);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void stillExilesTwoCardsWhenEgonLeavesBeforeItsTriggerResolves() {
        Permanent egon = harness.addToBattlefieldAndReturn(player1, new EgonGodOfDeath());
        Card first = new SnowCoveredForest();
        Card second = new DemonicGifts();
        Card drawCard = new DraugrRecruiter();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player2, List.of(new FeedTheSerpent()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player2, 0, egon.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawCard);
    }

    @Test
    void neitherFaceTriggersDuringOpponentsUpkeep() {
        Permanent egon = harness.addToBattlefieldAndReturn(player1, new EgonGodOfDeath());
        harness.addToBattlefield(player1, new ThroneOfDeath());
        Card libraryCard = new DemonicGifts();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(egon);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void throneCannotUseNoncreatureCardsOrOpponentsCreatureCardsToPayItsCost() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfDeath());
        Card noncreature = new DemonicGifts();
        Card opponentsCreature = new DraugrRecruiter();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(throne.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void thronePaysTapAndExileCostsBeforeDrawingAndCannotActivateAgainWhileTapped() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfDeath());
        Card creature = new DraugrRecruiter();
        Card otherCreature = new DraugrRecruiter();
        Card drawCard = new DemonicGifts();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setLibrary(player1, List.of(drawCard));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(throne.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawCard);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }
}
