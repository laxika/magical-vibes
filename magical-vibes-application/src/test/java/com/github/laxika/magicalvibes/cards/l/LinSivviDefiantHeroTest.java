package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.d.DefiantVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinSivviDefiantHeroTest extends BaseCardTest {

    @Test
    @DisplayName("The X ability offers Rebel permanents with mana value X or less")
    void searchesForRebelPermanentWithManaValueAtMostX() {
        addReadyLin();
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new DefiantFalcon(),
                new DefiantVanguard(),
                new HolyDay(),
                new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Defiant Falcon");
    }

    @Test
    @DisplayName("The X ability puts the chosen Rebel permanent onto the battlefield")
    void putsChosenRebelPermanentOntoBattlefield() {
        addReadyLin();
        harness.setLibrary(player1, new ArrayList<>(List.of(new DefiantFalcon())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Lin Sivvi, Defiant Hero", "Defiant Falcon");
    }

    @Test
    @DisplayName("The second ability puts a target Rebel card on the bottom of its owner's library")
    void putsTargetRebelCardOnBottomOfLibrary() {
        int linIndex = addReadyLin();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card rebel = new DefiantFalcon();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rebel)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HolyDay())));

        harness.activateAbilityWithGraveyardTargets(player1, linIndex, 1, List.of(rebel.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(rebel.getId());
    }

    @Test
    @DisplayName("The second ability cannot target a non-Rebel card")
    void rejectsNonRebelGraveyardTarget() {
        int linIndex = addReadyLin();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card nonRebel = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(nonRebel)));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, linIndex, 1, List.of(nonRebel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int addReadyLin() {
        harness.addToBattlefield(player1, new LinSivviDefiantHero());
        Permanent lin = findPermanent(player1, "Lin Sivvi, Defiant Hero");
        lin.setSummoningSick(false);
        return gd.playerBattlefields.get(player1.getId()).indexOf(lin);
    }
}
