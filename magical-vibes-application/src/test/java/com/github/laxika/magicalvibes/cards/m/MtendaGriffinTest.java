package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MtendaGriffin.class, EkunduGriffin.class, Maro.class})
class MtendaGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("During your upkeep, bounces itself and returns a Griffin card from your graveyard to hand")
    void bouncesSelfAndReturnsGriffin() {
        Permanent mtenda = addCreatureReady(player1, new MtendaGriffin());
        Card ekundu = new EkunduGriffin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ekundu)));
        enterUpkeep();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(ekundu.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(ekundu.getId(), mtenda.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(ekundu.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(mtenda.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a non-Griffin card in your graveyard")
    void cannotTargetNonGriffin() {
        Permanent mtenda = addCreatureReady(player1, new MtendaGriffin());
        Card maro = new Maro();
        harness.setGraveyard(player1, new ArrayList<>(List.of(maro)));
        enterUpkeep();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(maro.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(mtenda.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(maro.getId()));
    }

    @Test
    @DisplayName("Cannot target a Griffin in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Permanent mtenda = addCreatureReady(player1, new MtendaGriffin());
        Card ekundu = new EkunduGriffin();
        harness.setGraveyard(player2, new ArrayList<>(List.of(ekundu)));
        enterUpkeep();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(ekundu.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(mtenda.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot activate without a Griffin card in your graveyard")
    void requiresGriffinTarget() {
        Permanent mtenda = addCreatureReady(player1, new MtendaGriffin());
        enterUpkeep();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(mtenda.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot activate while the source is tapped")
    void cannotActivateWhileTapped() {
        Permanent mtenda = addCreatureReady(player1, new MtendaGriffin());
        Card ekundu = new EkunduGriffin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ekundu)));
        enterUpkeep();
        mtenda.tap();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(ekundu.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(mtenda.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot be activated outside your upkeep")
    void cannotActivateOutsideUpkeep() {
        Permanent mtenda = addCreatureReady(player1, new MtendaGriffin());
        Card ekundu = new EkunduGriffin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ekundu)));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(ekundu.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(mtenda.getCard().getId()));
    }

    private void enterUpkeep() {
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
