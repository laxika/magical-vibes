package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JunNEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {B}{B} sacrifices Junún Efreet")
    void decliningPaymentSacrificesEfreet() {
        harness.addToBattlefield(player1, new JunNEfreet());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Junún Efreet");
        harness.assertInGraveyard(player1, "Junún Efreet");
    }

    @Test
    @DisplayName("Paying {B}{B} keeps Junún Efreet on the battlefield")
    void payingKeepsEfreet() {
        harness.addToBattlefield(player1, new JunNEfreet());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Junún Efreet");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new JunNEfreet());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Junún Efreet");
    }
}
