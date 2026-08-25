package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrifterIlDal.class})
class DrifterIlDalTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {U} sacrifices Drifter il-Dal")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drifter il-Dal");
        harness.assertInGraveyard(player1, "Drifter il-Dal");
    }

    @Test
    @DisplayName("Paying {U} keeps Drifter il-Dal on the battlefield")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Drifter il-Dal");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without blue mana sacrifices Drifter il-Dal")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Drifter il-Dal");
    }

    @Test
    @DisplayName("Drifter il-Dal does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drifter il-Dal");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
