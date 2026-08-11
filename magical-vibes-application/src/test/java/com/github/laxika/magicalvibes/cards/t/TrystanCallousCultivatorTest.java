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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addFrontFace(Player player) {
        TrystanCallousCultivator card = new TrystanCallousCultivator();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
