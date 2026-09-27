package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpecimenCollector.class, Shock.class})
class SpecimenCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a Squirrel and a Crab")
    void enteringCreatesTokens() {
        castSpecimenCollector();

        Permanent squirrel = findPermanent(player1, "Squirrel");
        Permanent crab = findPermanent(player1, "Crab");
        assertThat(squirrel.getCard().getSubtypes()).containsExactly(CardSubtype.SQUIRREL);
        assertThat(crab.getCard().getSubtypes()).containsExactly(CardSubtype.CRAB);
        assertThat(squirrel.getCard().getPower()).isEqualTo(1);
        assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
        assertThat(crab.getCard().getPower()).isZero();
        assertThat(crab.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("When it dies, creates a copy of a target token you control")
    void deathCreatesTokenCopy() {
        castSpecimenCollector();
        Permanent squirrel = findPermanent(player1, "Squirrel");
        Permanent specimenCollector = findPermanent(player1, "Specimen Collector");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, specimenCollector.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(squirrel.getId(), findPermanent(player1, "Crab").getId());
        harness.handlePermanentChosen(player1, squirrel.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(2);
        assertThat(findPermanents(player1, "Crab")).hasSize(1);
    }

    @Test
    @DisplayName("The death trigger cannot target a nontoken or an opponent's token")
    void deathTriggerRequiresOwnToken() {
        castSpecimenCollector();
        Permanent specimenCollector = findPermanent(player1, "Specimen Collector");
        Permanent opponentToken = harness.addToBattlefieldAndReturn(player2, tokenCard());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, specimenCollector.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(findPermanent(player1, "Squirrel").getId(), findPermanent(player1, "Crab").getId())
                .doesNotContain(opponentToken.getId(), specimenCollector.getId());
    }

    private void castSpecimenCollector() {
        harness.setHand(player1, List.of(new SpecimenCollector()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card tokenCard() {
        Card card = new Card();
        card.setName("Opponent Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(true);
        return card;
    }
}
