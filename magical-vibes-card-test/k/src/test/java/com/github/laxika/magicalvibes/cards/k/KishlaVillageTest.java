package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KishlaVillage.class, Island.class, Swamp.class})
class KishlaVillageTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Island or Swamp")
    void entersTappedWithoutIslandOrSwamp() {
        harness.setHand(player1, List.of(new KishlaVillage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findVillage(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new KishlaVillage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findVillage(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new KishlaVillage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findVillage(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping adds one green mana")
    void tappingAddsGreenMana() {
        addVillageReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four mana and tapping surveils 2")
    void surveilsTwo() {
        GameData gameData = harness.getGameData();
        Permanent village = addVillageReady(player1);
        Card topCard = new Island();
        Card secondCard = new Swamp();
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        PendingInteraction.Scry surveil = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard, secondCard);
        assertThat(village.isTapped()).isTrue();
    }

    private Permanent addVillageReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KishlaVillage());
    }

    @Test
    void opponentsIslandAndSwampDoNotAllowUntappedEntry() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new KishlaVillage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findVillage(player1).isTapped()).isTrue();
    }

    @Test
    void surveilCanKeepBothCardsInReverseOrder() {
        Card first = new Island();
        Card second = new Swamp();
        Card third = new KishlaVillage();
        harness.setLibrary(player1, List.of(first, second, third));
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        activateSurveil();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
    }

    @Test
    void surveilCanKeepOneCardAndPutOneInGraveyard() {
        Card first = new Island();
        Card second = new Swamp();
        Card third = new KishlaVillage();
        harness.setLibrary(player1, List.of(first, second, third));
        activateSurveil();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second, third);
    }

    @Test
    void surveilWithOneCardOnlyLooksAtThatCard() {
        Card onlyCard = new Island();
        harness.setLibrary(player1, List.of(onlyCard));
        activateSurveil();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void surveilEmptyLibraryCompletesWithoutLosing() {
        harness.setLibrary(player1, List.of());
        activateSurveil();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playersWhoLostGameThisMatch).doesNotContain(player1.getId());
    }

    private void activateSurveil() {
        addVillageReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
    }

    private Permanent findVillage(Player player) {
        return findPermanent(player, "Kishla Village");
    }
}
