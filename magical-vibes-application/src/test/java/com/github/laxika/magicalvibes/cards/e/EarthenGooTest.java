package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EarthenGooTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep with red mana keeps Earthen Goo and boosts it")
    void paysWithRedMana() {
        Permanent goo = harness.addToBattlefieldAndReturn(player1, new EarthenGoo());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(goo.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, goo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goo)).isEqualTo(3);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goo);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Each age-counter payment can use either red or green mana")
    void paysEachAgeCounterWithEitherColor() {
        Permanent goo = harness.addToBattlefieldAndReturn(player1, new EarthenGoo());
        goo.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(goo.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, goo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goo)).isEqualTo(4);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goo);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Earthen Goo")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent goo = harness.addToBattlefieldAndReturn(player1, new EarthenGoo());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goo);
        harness.assertInGraveyard(player1, "Earthen Goo");
    }
}
