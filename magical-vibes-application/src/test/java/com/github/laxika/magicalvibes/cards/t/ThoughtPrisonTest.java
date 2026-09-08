package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtPrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles and imprints a chosen nonland card")
    void acceptsImprint() {
        CardChoiceSetup setup = castAndResolveEtb(List.of(new Peek(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(setup.chosenCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(setup.landCard());
        assertThat(gd.getImprintedCard(setup.prison())).isSameAs(setup.chosenCard());
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the hand unchanged and does not imprint")
    void declinesImprint() {
        CardChoiceSetup setup = castAndResolveEtb(List.of(new Peek()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(setup.chosenCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(setup.chosenCard());
        assertThat(gd.getImprintedCard(setup.prison())).isNull();
    }

    @Test
    @DisplayName("A spell sharing a color with the imprinted card damages its caster")
    void matchingColorDamagesCaster() {
        addPrisonWithImprint(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A spell sharing a mana value with the imprinted card damages its caster")
    void matchingManaValueDamagesCaster() {
        addPrisonWithImprint(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A matching spell cast by an opponent damages that opponent")
    void matchingOpponentSpellDamagesOpponent() {
        addPrisonWithImprint(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A spell sharing neither color nor mana value does not trigger the damage")
    void ignoresNonmatchingSpell() {
        addPrisonWithImprint(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private CardChoiceSetup castAndResolveEtb(List<Card> targetHand) {
        Card chosenCard = targetHand.getFirst();
        Card landCard = targetHand.size() > 1 ? targetHand.get(1) : null;
        ThoughtPrison prison = new ThoughtPrison();

        harness.setHand(player1, List.of(prison));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        return new CardChoiceSetup(prison, chosenCard, landCard);
    }

    private void addPrisonWithImprint(Card imprintedCard) {
        ThoughtPrison prison = new ThoughtPrison();
        harness.addToBattlefield(player1, prison);
        gd.setImprintedCard(prison, imprintedCard);
    }

    private record CardChoiceSetup(ThoughtPrison prison,
                                   Card chosenCard,
                                   Card landCard) {
    }
}
