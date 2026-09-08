package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnderworldCoinsmith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedtoothVanguard.class, UnderworldCoinsmith.class, GrizzlyBears.class})
class RedtoothVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} returns Redtooth Vanguard from the graveyard to hand")
    void payingManaReturnsToHand() {
        RedtoothVanguard vanguard = putVanguardInGraveyard();
        castEnchantmentCreature(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(vanguard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(vanguard);
    }

    @Test
    @DisplayName("Declining to pay keeps Redtooth Vanguard in the graveyard")
    void decliningKeepsItInGraveyard() {
        RedtoothVanguard vanguard = putVanguardInGraveyard();
        castEnchantmentCreature(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vanguard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(vanguard);
    }

    @Test
    @DisplayName("A non-enchantment entering does not trigger Redtooth Vanguard")
    void nonEnchantmentDoesNotTrigger() {
        putVanguardInGraveyard();
        prepareMain(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An enchantment an opponent controls entering does not trigger Redtooth Vanguard")
    void opponentEnchantmentDoesNotTrigger() {
        putVanguardInGraveyard();
        castEnchantmentCreature(player2);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private RedtoothVanguard putVanguardInGraveyard() {
        RedtoothVanguard vanguard = new RedtoothVanguard();
        harness.setGraveyard(player1, List.of(vanguard));
        return vanguard;
    }

    private void castEnchantmentCreature(Player controller) {
        prepareMain(controller);
        harness.setHand(controller, List.of(new UnderworldCoinsmith()));
        harness.addMana(controller, ManaColor.WHITE, 1);
        harness.addMana(controller, ManaColor.BLACK, 1);

        harness.castCreature(controller, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
