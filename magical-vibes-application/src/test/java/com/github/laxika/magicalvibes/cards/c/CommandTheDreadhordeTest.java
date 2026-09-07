package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandTheDreadhorde.class, GrizzlyBears.class, HillGiant.class, HolyDay.class})
class CommandTheDreadhordeTest extends BaseCardTest {

    @Test
    void damagesControllerForTotalManaValueAndReturnsCardsUnderTheirControl() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(ownCreature.getId(), opponentCreature.getId());
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opponentCreature.getId()));
    }

    @Test
    void mayChooseNoTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    void cannotTargetNonCreatureNonPlaneswalkerCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), instant));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
