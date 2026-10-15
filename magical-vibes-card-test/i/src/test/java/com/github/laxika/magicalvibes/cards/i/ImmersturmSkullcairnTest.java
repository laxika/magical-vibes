package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmersturmSkullcairn.class, AxgardCavalry.class})
class ImmersturmSkullcairnTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ImmersturmSkullcairn()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one black mana")
    void tapAddsBlackMana() {
        addReadySkullcairn(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Immersturm Skullcairn");
    }

    @Test
    @DisplayName("Sacrifice ability deals damage and makes the target player discard")
    void sacrificeAbilityDealsDamageAndDiscards() {
        addReadySkullcairn(player1);
        harness.setHand(player2, List.of(new AxgardCavalry()));
        addSacrificeAbilityMana();

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Immersturm Skullcairn");
        harness.assertInGraveyard(player1, "Immersturm Skullcairn");
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Axgard Cavalry");
    }

    @Test
    @DisplayName("Sacrifice ability can target its controller")
    void sacrificeAbilityCanTargetController() {
        addReadySkullcairn(player1);
        harness.setHand(player1, List.of(new AxgardCavalry()));
        addSacrificeAbilityMana();

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Axgard Cavalry");
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated at sorcery speed")
    void sacrificeAbilityIsSorcerySpeedOnly() {
        addReadySkullcairn(player1);
        addSacrificeAbilityMana();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a permanent")
    void sacrificeAbilityCannotTargetPermanent() {
        addReadySkullcairn(player1);
        harness.addToBattlefield(player2, new AxgardCavalry());
        addSacrificeAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, harness.getPermanentId(player2, "Axgard Cavalry")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty hand does not prevent the damage")
    void damagesPlayerWithEmptyHand() {
        addReadySkullcairn(player1);
        harness.setHand(player2, List.of());
        addSacrificeAbilityMana();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Immersturm Skullcairn");
    }

    @Test
    @DisplayName("A tapped Skullcairn cannot pay the tap cost")
    void tappedLandCannotActivateSacrificeAbility() {
        addReadySkullcairn(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        addSacrificeAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Immersturm Skullcairn");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice ability cannot be activated outside a main phase")
    void cannotActivateDuringCombat() {
        addReadySkullcairn(player1);
        addSacrificeAbilityMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Immersturm Skullcairn");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice ability requires both red mana")
    void cannotActivateWithoutEnoughRedMana() {
        addReadySkullcairn(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Immersturm Skullcairn");
        assertThat(gd.stack).isEmpty();
    }

    private void addReadySkullcairn(Player player) {
        harness.addToBattlefield(player, new ImmersturmSkullcairn());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addSacrificeAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
