package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReunionOfTheHouse.class, GrizzlyBears.class, HillGiant.class})
class ReunionOfTheHouseTest extends BaseCardTest {

    @Test
    @DisplayName("Returns targeted creature cards with total power 10 or less and exiles itself")
    void returnsCreatureCardsWithinTotalPowerLimit() {
        Card bear = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card secondBear = new GrizzlyBears();
        bear.setPower(2);
        hillGiant.setPower(3);
        secondBear.setPower(2);
        harness.setGraveyard(player1, List.of(bear, hillGiant, secondBear));
        prepareSpell();

        harness.castSorcery(player1, 0,
                List.of(bear.getId(), hillGiant.getId(), secondBear.getId()));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(bear.getId(), hillGiant.getId(), secondBear.getId());
        assertThat(gameData.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Reunion of the House"));
    }

    @Test
    @DisplayName("Rejects a target selection whose total power exceeds 10")
    void rejectsSelectionAboveTotalPowerLimit() {
        List<Card> hillGiants = List.of(new HillGiant(), new HillGiant(), new HillGiant(), new HillGiant());
        hillGiants.forEach(card -> card.setPower(3));
        harness.setGraveyard(player1, hillGiants);
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                hillGiants.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power cannot exceed 10");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ReunionOfTheHouse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
