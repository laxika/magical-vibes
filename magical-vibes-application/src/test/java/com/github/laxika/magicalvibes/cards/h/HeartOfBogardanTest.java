package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeartOfBogardanTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Heart of Bogardan and deals no damage")
    void payingUpkeepKeepsIt() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        harness.setLife(player2, 20);
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(heart.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heart);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining on the first upkeep sacrifices it for 0 damage (twice one age counter minus 2)")
    void firstUpkeepDealsNoDamage() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        harness.setLife(player2, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(heart);
        harness.assertInGraveyard(player1, "Heart of Bogardan");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Declining on the third upkeep deals 4 damage to the opponent and their creatures")
    void thirdUpkeepDealsFourDamage() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new HeartOfBogardan());
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(heart.getCounterCount(CounterType.AGE)).isEqualTo(3);
        harness.handleMayAbilityChosen(player1, false);

        // X = 2 * 3 - 2 = 4: lethal to the 2/2 and 4 off the opponent's life total.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(heart);
    }
}
