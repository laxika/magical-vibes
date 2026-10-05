package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MisterImmortal.class)
class MisterImmortalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard to the battlefield tapped")
    void returnsFromGraveyardTapped() {
        MisterImmortal misterImmortal = new MisterImmortal();
        harness.setGraveyard(player1, List.of(misterImmortal));
        prepareActivation();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(misterImmortal.getId()));
        assertReturnedTapped(misterImmortal);
    }

    @Test
    @DisplayName("Returns from exile to the battlefield tapped")
    void returnsFromExileTapped() {
        MisterImmortal misterImmortal = new MisterImmortal();
        harness.setExile(player1, List.of(misterImmortal));
        prepareActivation();

        harness.activateExileAbility(player1, misterImmortal.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(misterImmortal.getId())).isNull();
        assertReturnedTapped(misterImmortal);
    }

    @Test
    @DisplayName("Returns from the graveyard during combat")
    void returnsFromGraveyardDuringCombat() {
        MisterImmortal card = new MisterImmortal();
        harness.setGraveyard(player1, List.of(card));
        prepareActivation();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertReturnedTapped(card);
    }

    @Test
    @DisplayName("Returns from exile during combat")
    void returnsFromExileDuringCombat() {
        MisterImmortal card = new MisterImmortal();
        harness.setExile(player1, List.of(card));
        prepareActivation();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateExileAbility(player1, card.getId());
        harness.passBothPriorities();
        assertReturnedTapped(card);
    }

    @Test
    @DisplayName("Returns from the graveyard during the opponent's turn")
    void returnsFromGraveyardDuringOpponentsTurn() {
        MisterImmortal card = new MisterImmortal();
        harness.setGraveyard(player1, List.of(card));
        prepareActivation();
        harness.forceActivePlayer(player2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertReturnedTapped(card);
    }

    @Test
    @DisplayName("Returns from exile during the opponent's turn")
    void returnsFromExileDuringOpponentsTurn() {
        MisterImmortal card = new MisterImmortal();
        harness.setExile(player1, List.of(card));
        prepareActivation();
        harness.forceActivePlayer(player2);

        harness.activateExileAbility(player1, card.getId());
        harness.passBothPriorities();
        assertReturnedTapped(card);
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void assertReturnedTapped(MisterImmortal card) {
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }
}
