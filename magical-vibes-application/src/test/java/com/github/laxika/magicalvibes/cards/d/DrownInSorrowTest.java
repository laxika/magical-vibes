package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DrownInSorrowTest extends BaseCardTest {

    @Test
    @DisplayName("Gives every creature -2/-2, including the caster's own")
    void weakensAllCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castDrownInSorrow();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(6);

        keepScryCardOnTop();
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castDrownInSorrow();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(8);
    }

    @Test
    @DisplayName("Scry 1 begins after the creatures are weakened")
    void scriesAfterWeakeningCreatures() {
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castDrownInSorrow();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(6);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards().get(0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castDrownInSorrow() {
        harness.setHand(player1, List.of(new DrownInSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void keepScryCardOnTop() {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
