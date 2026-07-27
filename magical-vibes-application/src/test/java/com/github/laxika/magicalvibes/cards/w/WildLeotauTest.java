package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WildLeotauTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {G} sacrifices Wild Leotau")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new WildLeotau());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Wild Leotau");
        harness.assertInGraveyard(player1, "Wild Leotau");
    }

    @Test
    @DisplayName("Paying {G} keeps Wild Leotau on the battlefield and spends the mana")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new WildLeotau());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.GREEN, 1); // mana empties between steps — add it at payment time
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Wild Leotau");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Accepting without green mana still sacrifices Wild Leotau")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new WildLeotau());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // can't actually pay {G}

        harness.assertNotOnBattlefield(player1, "Wild Leotau");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new WildLeotau());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Leotau");
    }
}
