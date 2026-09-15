package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.SoulChanneling;
import com.github.laxika.magicalvibes.cards.s.SnortingGahr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Undertaker.class, SnortingGahr.class, SoulChanneling.class})
class UndertakerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from the graveyard to hand after discarding a card")
    void returnsTargetCreatureCardToHand() {
        var undertaker = addCreatureReady(player1, new Undertaker());
        Card returnedCreature = new SnortingGahr();
        Card discardedCard = new SoulChanneling();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, returnedCreature.getId(), Zone.GRAVEYARD);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(undertaker.isTapped()).isTrue();
        harness.assertInHand(player1, "Snorting Gahr");
        harness.assertNotInGraveyard(player1, "Snorting Gahr");
        harness.assertInGraveyard(player1, "Soul Channeling");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        addCreatureReady(player1, new Undertaker());
        Card target = new SoulChanneling();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SoulChanneling()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new Undertaker());
        Card target = new SnortingGahr();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetCreatureInOpponentsGraveyard() {
        addCreatureReady(player1, new Undertaker());
        Card target = new SnortingGahr();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new SoulChanneling()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the targeted creature card leaves the graveyard before resolution")
    void fizzlesWhenTargetLeavesGraveyard() {
        var undertaker = addCreatureReady(player1, new Undertaker());
        Card target = new SnortingGahr();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SoulChanneling()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handleCardChosen(player1, 0);
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(undertaker.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Snorting Gahr");
        harness.assertInGraveyard(player1, "Soul Channeling");
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
