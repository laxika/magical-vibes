package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleChoiceTest extends BaseCardTest {

    @Test
    void xOneScriesThenDraws() {
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    void xTwoLetsChosenPlayerReturnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice playerChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerChoice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void xThreeCreatesElemental() {
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
        assertThat(elemental.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(elemental.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    void xFourDoesAllThreeModes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
