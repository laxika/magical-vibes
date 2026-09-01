package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveyardShift.class, GrizzlyBears.class, HillGiant.class, Mountain.class, Murder.class, Shock.class})
class GraveyardShiftTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from your graveyard to the battlefield")
    void returnsTargetCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GraveyardShift()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can be cast at instant speed with five distinct mana values in your graveyard")
    void fiveDistinctManaValuesGrantFlashTiming() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(
                new Mountain(), new Shock(), target, new Murder(), new HillGiant()));
        castDuringOpponentsTurn(target);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without five distinct mana values")
    void fewerThanFiveDistinctManaValuesKeepSorceryTiming() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Mountain(), new Shock(), target, new Murder()));
        prepareToCastDuringOpponentsTurn();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castDuringOpponentsTurn(Card target) {
        prepareToCastDuringOpponentsTurn();
        harness.castSorcery(player1, 0, target.getId());
    }

    private void prepareToCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GraveyardShift()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.getGameService().passPriority(harness.getGameData(), player2);
    }
}
