package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DireFleetDaredevilTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets only an opponent's instant or sorcery card")
    void etbTargetsOpponentInstantOrSorcery() {
        Divination ownDivination = new Divination();
        Divination opponentDivination = new Divination();
        GrizzlyBears opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownDivination));
        harness.setGraveyard(player2, List.of(opponentDivination, opponentCreature));

        castDaredevil();

        PendingInteraction.MultiGraveyardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentDivination.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentDivination.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().findExiledCard(opponentDivination.getId())).isNotNull();
        assertThat(harness.getGameData().findExiledCard(ownDivination.getId())).isNull();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("ETB grants this-turn casting with any mana and exiles the spell afterward")
    void etbSpellCanBeCastWithAnyManaAndIsExiledAfterward() {
        Divination divination = new Divination();
        harness.setGraveyard(player2, List.of(divination));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        castDaredevil();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().exilePlayAnyManaType).contains(divination.getId());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, divination.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().findExiledCard(divination.getId())).isNotNull();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).doesNotContain(divination);
    }

    @Test
    @DisplayName("ETB does not prompt when opponents have no instant or sorcery cards")
    void etbHasNoTargetForNonSpellCards() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castDaredevil();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
    }

    private void castDaredevil() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DireFleetDaredevil()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
