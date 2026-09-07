package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarigaazTheIgniter.class, AirElemental.class, GrizzlyBears.class})
class DarigaazTheIgniterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage payment reveals the hand and deals damage for cards of the chosen color")
    void paidAbilityDealsDamageForChosenColorCards() {
        harness.setLife(player2, 20);
        addAttackingDarigaaz();
        harness.setHand(player2, new ArrayList<>(List.of(new AirElemental(), new GrizzlyBears())));

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("Air Elemental")
                && log.plainText().contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Choosing a color with no matching cards deals no additional damage")
    void noMatchingCardsDealNoAdditionalDamage() {
        harness.setLife(player2, 20);
        addAttackingDarigaaz();
        harness.setHand(player2, new ArrayList<>(List.of(new AirElemental())));

        resolveCombatToMayPrompt();
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Declining the payment does not reveal the hand or deal additional damage")
    void decliningPaymentDoesNothing() {
        harness.setLife(player2, 20);
        addAttackingDarigaaz();
        harness.setHand(player2, new ArrayList<>(List.of(new AirElemental())));

        resolveCombatToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.gameLog).noneMatch(log -> log.plainText().contains("reveals their hand"));
    }

    @Test
    @DisplayName("Blocked Darigaaz does not trigger the ability")
    void blockedDarigaazDoesNotTrigger() {
        addAttackingDarigaaz();
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new AirElemental())));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private Permanent addAttackingDarigaaz() {
        Permanent darigaaz = addCreatureReady(player1, new DarigaazTheIgniter());
        darigaaz.setAttacking(true);
        return darigaaz;
    }

    private void resolveCombatToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addPaymentMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
