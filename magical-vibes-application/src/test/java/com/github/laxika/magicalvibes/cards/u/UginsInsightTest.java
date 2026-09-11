package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UginsInsight.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class UginsInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Scries for the greatest mana value among your permanents, then draws three cards")
    void scriesForGreatestControlledManaValueThenDrawsThree() {
        harness.setLibrary(player1, List.of(
                new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        castUginInsight();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(5);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                List.of(0, 1, 2, 3, 4), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws three cards without permanents")
    void drawsThreeWithoutPermanents() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castUginInsight();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private void castUginInsight() {
        harness.setHand(player1, List.of(new UginsInsight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
