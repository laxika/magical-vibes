package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FiremaneAngel.class, LastGasp.class})
class FiremaneAngelTest extends BaseCardTest {

    @Test
    @DisplayName("May gain 1 life from the battlefield during your upkeep")
    void mayGainLifeFromBattlefield() {
        harness.addToBattlefield(player1, new FiremaneAngel());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("May decline life gain from the battlefield during your upkeep")
    void mayDeclineLifeGainFromBattlefield() {
        harness.addToBattlefield(player1, new FiremaneAngel());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("May gain 1 life from the graveyard during your upkeep")
    void mayGainLifeFromGraveyard() {
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life if it returns from the graveyard before its trigger resolves")
    void doesNotGainLifeIfReturnedBeforeGraveyardTriggerResolves() {
        FiremaneAngel angel = new FiremaneAngel();
        harness.setGraveyard(player1, List.of(angel));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Returns itself from the graveyard to the battlefield during upkeep")
    void returnsFromGraveyardToBattlefield() {
        FiremaneAngel angel = new FiremaneAngel();
        harness.setGraveyard(player1, List.of(angel));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(angel.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(angel.getId()));
    }

    @Test
    @DisplayName("Returns only the activated copy from the graveyard")
    void returnsOnlyTheActivatedCopyFromGraveyard() {
        FiremaneAngel otherAngel = new FiremaneAngel();
        FiremaneAngel activatedAngel = new FiremaneAngel();
        harness.setGraveyard(player1, List.of(otherAngel, activatedAngel));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(activatedAngel.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(otherAngel);
    }

    @Test
    @DisplayName("Cannot return itself from the graveyard outside its controller's upkeep")
    void cannotActivateOutsideUpkeep() {
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return itself during an opponent's upkeep")
    void cannotActivateDuringOpponentUpkeep() {
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }

    @Test
    @DisplayName("May decline life gain from the graveyard")
    void mayDeclineLifeGainFromGraveyard() {
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Neither battlefield nor graveyard copies trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new FiremaneAngel());
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life if it dies before its battlefield trigger resolves")
    void doesNotGainLifeIfItDiesBeforeBattlefieldTriggerResolves() {
        var angel = harness.addToBattlefieldAndReturn(player1, new FiremaneAngel());
        harness.setHand(player1, List.of(new LastGasp()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, angel.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Firemane Angel");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot return itself with insufficient generic mana")
    void cannotActivateWithInsufficientMana() {
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Firemane Angel");
        harness.assertNotOnBattlefield(player1, "Firemane Angel");
    }

    @Test
    @DisplayName("Generic mana cannot replace the required second white mana")
    void cannotActivateWithoutRequiredColoredMana() {
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Firemane Angel");
        harness.assertNotOnBattlefield(player1, "Firemane Angel");
    }
}
