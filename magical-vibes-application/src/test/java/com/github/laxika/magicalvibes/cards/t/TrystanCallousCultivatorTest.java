package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrystanCallousCultivator.class, Forest.class, LlanowarElves.class})
class TrystanCallousCultivatorTest extends BaseCardTest {

    @Test
    @DisplayName("The front face mills three and gains life when the graveyard contains an Elf")
    void frontFaceMillsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TrystanCallousCultivator()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves(), new Forest(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The back-face trigger exiles an Elf and makes each opponent lose life")
    void backFaceExilesElfAndOpponentsLoseLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new LlanowarElves())));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent trystan = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));

        assertThat(trystan.isTransformed()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof LlanowarElves);
    }

    @Test
    @DisplayName("The back-face trigger may decline to exile an Elf")
    void backFaceMayDeclineExile() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        LlanowarElves firstElf = new LlanowarElves();
        LlanowarElves secondElf = new LlanowarElves();
        harness.setGraveyard(player1, new ArrayList<>(List.of(firstElf, secondElf)));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent trystan = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(trystan.isTransformed()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstElf.getId(), secondElf.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The back-face trigger exiles the selected Elf when several are available")
    void backFaceExilesSelectedElf() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        LlanowarElves firstElf = new LlanowarElves();
        LlanowarElves secondElf = new LlanowarElves();
        harness.setGraveyard(player1, new ArrayList<>(List.of(firstElf, secondElf)));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent trystan = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstElf.getId()));

        assertThat(trystan.isTransformed()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(firstElf.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(secondElf.getId());
    }

    @Test
    @DisplayName("The back-face trigger may decline the only available Elf")
    void backFaceMayDeclineOnlyElf() {
        LlanowarElves elf = new LlanowarElves();
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(elf));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent trystan = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(trystan.isTransformed()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elf);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Elf already in the controller's graveyard grants life after milling non-Elves")
    void frontFaceCountsExistingElf() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new TrystanCallousCultivator());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("An opponent's Elf does not grant life or permit back-face exile")
    void opponentElfDoesNotQualify() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        Permanent trystan = harness.enterBattlefieldAndReturn(player1, new TrystanCallousCultivator());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(trystan.isTransformed()).isTrue();
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the black payment leaves the front face unchanged and does not mill")
    void mayDeclineTransformation() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent trystan = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(trystan.isTransformed()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Paying green on the back face transforms it to the front, mills, and gains life")
    void backFaceTransformsToFrontAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new LlanowarElves(), new Forest()));
        Permanent trystan = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(trystan.isTransformed()).isTrue();

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(trystan.isTransformed()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The back face can exile an Elf from the cards it just milled")
    void backFaceCanExileNewlyMilledElf() {
        LlanowarElves elf = new LlanowarElves();
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(elf, new LlanowarElves(), new Forest()));
        addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(elf.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).doesNotContain(elf);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A short library still mills its remaining Elf and grants life")
    void frontFaceMillsShortLibrary() {
        LlanowarElves elf = new LlanowarElves();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(elf));

        harness.enterBattlefieldAndReturn(player1, new TrystanCallousCultivator());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(elf);
        harness.assertLife(player1, 22);
    }

    private Permanent addFrontFace(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TrystanCallousCultivator());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
