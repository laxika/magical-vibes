package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.e.EmberBeast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbarianLunatic.class, DuskImp.class, EmberBeast.class})
class BarbarianLunaticTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to target creature")
    void sacrificesItselfAndDealsDamageToCreature() {
        addCreatureReady(player1, new BarbarianLunatic());
        harness.addToBattlefield(player2, new DuskImp());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent target = findPermanent(player2, "Dusk Imp");
        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Barbarian Lunatic");
        harness.assertInGraveyard(player1, "Barbarian Lunatic");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dusk Imp");
        harness.assertInGraveyard(player2, "Dusk Imp");
    }

    @Test
    @DisplayName("Deals exactly 2 damage to a creature that survives")
    void dealsExactlyTwoDamageToTargetCreature() {
        addCreatureReady(player1, new BarbarianLunatic());
        Permanent target = addCreatureReady(player1, new EmberBeast());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Ember Beast");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new BarbarianLunatic());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Barbarian Lunatic")).isNotNull();
    }
}
