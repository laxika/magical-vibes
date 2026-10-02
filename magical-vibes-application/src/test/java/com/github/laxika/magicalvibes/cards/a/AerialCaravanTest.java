package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialCaravan.class, DrakeHatchling.class, Island.class})
class AerialCaravanTest extends BaseCardTest {

    @Test
    @DisplayName("May play the exiled top card by paying its normal cost")
    void mayPlayExiledTopCardByPayingNormalCost() {
        addReadyCaravan();
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drake Hatchling");
    }

    @Test
    @DisplayName("Requires the exiled spell's normal mana cost")
    void requiresNormalManaToPlayExiledSpell() {
        addReadyCaravan();
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("May play an exiled land")
    void mayPlayExiledLand() {
        addReadyCaravan();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryExilesNothing() {
        addReadyCaravan();
        harness.setLibrary(player1, List.of());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        addReadyCaravan();
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Cannot play an exiled land after using the turn's land play")
    void exiledLandRespectsLandPlayLimit() {
        addReadyCaravan();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Island()));
        harness.playLand(player1, 0);
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot play an exiled land during the end step")
    void exiledLandRespectsMainPhaseTiming() {
        addReadyCaravan();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.END_STEP);
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Cannot play an exiled land while another ability is on the stack")
    void exiledLandRequiresEmptyStack() {
        addReadyCaravan();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot play an exiled land during the opponent's turn")
    void exiledLandRequiresControllersTurn() {
        addReadyCaravan();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        gd.activePlayerId = player2.getId();
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        var caravan = harness.addToBattlefieldAndReturn(player1, new AerialCaravan());
        caravan.setSummoningSick(true);
        caravan.tap();
        Card firstCard = new Island();
        Card secondCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCard, secondCard);
        harness.castFromExile(player1, firstCard.getId());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, secondCard.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Drake Hatchling");
    }

    @Test
    @DisplayName("An exiled creature still requires normal spell timing")
    void exiledCreatureRequiresNormalTiming() {
        addReadyCaravan();
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.END_STEP);
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    private void addReadyCaravan() {
        addCreatureReady(player1, new AerialCaravan());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
