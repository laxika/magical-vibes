package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.d.DefiantVanguard;
import com.github.laxika.magicalvibes.cards.d.Daze;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LinSivviDefiantHero.class, DefiantFalcon.class, DefiantVanguard.class, Daze.class, Mossdog.class})
class LinSivviDefiantHeroTest extends BaseCardTest {

    @Test
    @DisplayName("The X ability offers Rebel permanents with mana value X or less")
    void searchesForRebelPermanentWithManaValueAtMostX() {
        addReadyLin();
        harness.setLibrary(player1, List.of(
                new DefiantFalcon(),
                new DefiantVanguard(),
                new Daze(),
                new Mossdog()));
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
        harness.setLibrary(player1, List.of(new DefiantFalcon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Lin Sivvi, Defiant Hero", "Defiant Falcon");
    }

    @Test
    @DisplayName("The X ability may decline to find a matching Rebel")
    void mayDeclineToFindRebel() {
        int linIndex = addReadyLin();
        Card rebel = new DefiantFalcon();
        harness.setLibrary(player1, List.of(rebel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, linIndex, 0, 2, null);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(linIndex).isTapped()).isTrue();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().canFailToFind()).isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Lin Sivvi, Defiant Hero");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(rebel.getId());
    }

    @Test
    @DisplayName("The second ability puts a target Rebel card on the bottom of your library")
    void putsTargetRebelCardOnBottomOfLibrary() {
        int linIndex = addReadyLin();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card rebel = new DefiantFalcon();
        harness.setGraveyard(player1, List.of(rebel));
        harness.setLibrary(player1, List.of(new Daze()));

        harness.activateAbilityWithGraveyardTargets(player1, linIndex, 1, List.of(rebel.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(rebel.getId());
    }

    @Test
    @DisplayName("The second ability can be activated twice without tapping Lin Sivvi")
    void activatesTwiceWithoutTapping() {
        int linIndex = addReadyLin();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        Card first = new DefiantFalcon();
        Card second = new DefiantFalcon();
        Card filler = new Daze();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(filler));

        harness.activateAbilityWithGraveyardTargets(player1, linIndex, 1, List.of(first.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, linIndex, 1, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(filler.getId(), first.getId(), second.getId());
    }

    @Test
    @DisplayName("The second ability cannot target a non-Rebel card")
    void rejectsNonRebelGraveyardTarget() {
        int linIndex = addReadyLin();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card nonRebel = new Mossdog();
        harness.setGraveyard(player1, List.of(nonRebel));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, linIndex, 1, List.of(nonRebel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second ability cannot target a Rebel card in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        int linIndex = addReadyLin();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card rebel = new DefiantFalcon();
        harness.setGraveyard(player2, List.of(rebel));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, linIndex, 1, List.of(rebel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The X ability finds no positive-mana-value Rebel when X is zero")
    void xZeroFindsNoPositiveManaValueRebel() {
        int linIndex = addReadyLin();
        Card rebel = new DefiantFalcon();
        harness.setLibrary(player1, List.of(rebel));

        harness.activateAbility(player1, linIndex, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(rebel.getId());
    }

    private int addReadyLin() {
        Permanent lin = addCreatureReady(player1, new LinSivviDefiantHero());
        return gd.playerBattlefields.get(player1.getId()).indexOf(lin);
    }
}
