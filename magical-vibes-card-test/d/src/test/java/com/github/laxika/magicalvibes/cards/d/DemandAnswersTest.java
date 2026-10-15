package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Cryptex;
import com.github.laxika.magicalvibes.cards.s.SanitationAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemandAnswers.class, Forest.class, SanitationAutomaton.class, Cryptex.class})
class DemandAnswersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact lets you draw two cards")
    void sacrificesArtifactAndDrawsTwoCards() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        harness.setHand(player1, List.of(new DemandAnswers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanitation Automaton");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Discarding a card lets you draw two cards")
    void discardsCardAndDrawsTwoCards() {
        harness.setHand(player1, List.of(new DemandAnswers(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast without an artifact or another card to discard")
    void cannotCastWithoutPaymentOption() {
        harness.setHand(player1, List.of(new DemandAnswers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void sacrificesNoncreatureArtifactBeforeDrawing() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Cryptex());
        harness.setHand(player1, List.of(new DemandAnswers(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());

        harness.assertInGraveyard(player1, "Cryptex");
        harness.assertNotOnBattlefield(player1, "Cryptex");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Demand Answers");
    }

    @Test
    void canChooseDiscardWithAnArtifactAvailableAndCardBeforeSpellInHand() {
        harness.addToBattlefield(player1, new Cryptex());
        harness.setHand(player1, List.of(new Forest(), new DemandAnswers()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithDiscard(player1, 1, null, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Cryptex");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Cryptex");
    }

    @Test
    void cannotSacrificeNonartifactEvenWhenDiscardIsAvailable() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DemandAnswers(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsArtifactEvenWhenDiscardIsAvailable() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Cryptex());
        harness.setHand(player1, List.of(new DemandAnswers(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Cryptex");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDiscardTheSpellItself() {
        harness.setHand(player1, List.of(new DemandAnswers(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
