package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MausoleumTurnkey.class, GrayscaledGharial.class, LastGasp.class})
class MausoleumTurnkeyTest extends BaseCardTest {

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MausoleumTurnkey(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The opponent chooses a creature card from the controller's graveyard")
    void opponentChoosesCreatureCard() {
        GrayscaledGharial creature = new GrayscaledGharial();
        LastGasp noncreature = new LastGasp();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        castAndResolveEtb();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .hasMessageContaining("Not your turn");

        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grayscaled Gharial");
        harness.assertInGraveyard(player1, "Last Gasp");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent chooses exactly one of multiple creature cards")
    void opponentChoosesAmongMultipleCreatureCards() {
        GrayscaledGharial firstCreature = new GrayscaledGharial();
        GrayscaledGharial secondCreature = new GrayscaledGharial();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        castAndResolveEtb();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(firstCreature.getId(), secondCreature.getId());

        harness.handleMultipleCardsChosen(player2, List.of(secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No creature card in the graveyard produces no choice")
    void noCreatureCardDoesNothing() {
        harness.setGraveyard(player1, List.of(new LastGasp()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mausoleum Turnkey");
        harness.assertInGraveyard(player1, "Last Gasp");
    }
}
