package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.s.SandbarMerfolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Exhume.class, SandbarMerfolk.class, DarkRitual.class})
class ExhumeTest extends BaseCardTest {

    @Test
    @DisplayName("Each player returns one creature card from their graveyard")
    void eachPlayerReturnsOneCreatureCard() {
        Card player1Creature = new SandbarMerfolk();
        Card player2Creature = new SandbarMerfolk();
        harness.setGraveyard(player1, List.of(player1Creature));
        harness.setGraveyard(player2, List.of(player2Creature));

        castExhume();

        assertThat(battlefieldCards(player1)).containsExactly(player1Creature);
        assertThat(battlefieldCards(player2)).containsExactly(player2Creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(player2Creature);
    }

    @Test
    @DisplayName("Leaves noncreature cards and additional creature cards in graveyards")
    void returnsOnlyOneCreaturePerPlayer() {
        Card returned = new SandbarMerfolk();
        Card remainingCreature = new SandbarMerfolk();
        Card instant = new DarkRitual();
        harness.setGraveyard(player1, List.of(returned, remainingCreature, instant));

        castExhume();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(battlefieldCards(player1)).containsExactly(returned);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(remainingCreature, instant)
                .doesNotContain(returned);
    }

    @Test
    @DisplayName("Waits for every player to choose before returning any creatures")
    void waitsForEveryPlayerToChooseBeforeReturningCreatures() {
        Card player1First = new SandbarMerfolk();
        Card player1Second = new SandbarMerfolk();
        Card player2Creature = new SandbarMerfolk();
        harness.setGraveyard(player1, List.of(player1First, player1Second));
        harness.setGraveyard(player2, List.of(player2Creature));

        castExhume();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(battlefieldCards(player1)).isEmpty();
        assertThat(battlefieldCards(player2)).isEmpty();

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(battlefieldCards(player1)).containsExactly(player1First);
        assertThat(battlefieldCards(player2)).containsExactly(player2Creature);
    }

    @Test
    @DisplayName("Puts chosen creatures onto the battlefield simultaneously")
    void putsChosenCreaturesOntoBattlefieldSimultaneously() {
        Card player1First = new SandbarMerfolk();
        Card player1Second = new SandbarMerfolk();
        Card player2First = new SandbarMerfolk();
        Card player2Second = new SandbarMerfolk();
        harness.setGraveyard(player1, List.of(player1First, player1Second));
        harness.setGraveyard(player2, List.of(player2First, player2Second));

        castExhume();

        assertThat(battlefieldCards(player1)).isEmpty();
        assertThat(battlefieldCards(player2)).isEmpty();

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(battlefieldCards(player1)).isEmpty();
        assertThat(battlefieldCards(player2)).isEmpty();

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(battlefieldCards(player1)).containsExactly(player1First);
        assertThat(battlefieldCards(player2)).containsExactly(player2First);
    }

    private void castExhume() {
        harness.setHand(player1, List.of(new Exhume()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<Card> battlefieldCards(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(permanent -> permanent.getCard())
                .toList();
    }
}
