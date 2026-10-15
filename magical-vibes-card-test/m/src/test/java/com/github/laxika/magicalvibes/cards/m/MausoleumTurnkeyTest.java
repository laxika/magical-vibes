package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    @Test
    @DisplayName("Creature cards in the opponent's graveyard are not eligible")
    void ignoresOpponentsGraveyard() {
        GrayscaledGharial ownCreature = new GrayscaledGharial();
        GrayscaledGharial opposingCreature = new GrayscaledGharial();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        castAndResolveEtb();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player2, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    @DisplayName("The target is chosen before resolution and is not replaced if it leaves the graveyard")
    void chosenTargetLeavingGraveyardDoesNotReturnAnotherCard() {
        GrayscaledGharial chosenCreature = new GrayscaledGharial();
        GrayscaledGharial otherCreature = new GrayscaledGharial();
        harness.setGraveyard(player1, List.of(chosenCreature, otherCreature));
        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player2, List.of(chosenCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(chosenCreature, otherCreature);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setExile(player1, List.of(chosenCreature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.findExiledCard(chosenCreature.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The chosen card returns even if Turnkey dies in response to its trigger")
    void triggerResolvesAfterSourceDies() {
        GrayscaledGharial creature = new GrayscaledGharial();
        harness.setGraveyard(player1, List.of(creature));
        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));

        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Mausoleum Turnkey"));
        harness.assertNotOnBattlefield(player1, "Mausoleum Turnkey");
        harness.assertInGraveyard(player1, "Mausoleum Turnkey");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Mausoleum Turnkey");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
