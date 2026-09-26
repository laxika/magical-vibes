package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiresOfYavimaya.class, YavimayaBarbarian.class})
class FiresOfYavimayaTest extends BaseCardTest {

    @Test
    @DisplayName("Fires of Yavimaya grants haste to creatures its controller controls")
    void grantsHasteToOwnCreatures() {
        harness.addToBattlefield(player1, new FiresOfYavimaya());
        harness.addToBattlefield(player1, new YavimayaBarbarian());
        harness.addToBattlefield(player2, new YavimayaBarbarian());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Yavimaya Barbarian"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Yavimaya Barbarian"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing Fires of Yavimaya gives a target creature +2/+2 until end of turn")
    void sacrificeAbilityBoostsTarget() {
        harness.addToBattlefield(player1, new FiresOfYavimaya());
        harness.addToBattlefield(player2, new YavimayaBarbarian());
        Permanent target = findPermanent(player2, "Yavimaya Barbarian");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fires of Yavimaya");
        harness.assertInGraveyard(player1, "Fires of Yavimaya");
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The sacrifice ability can target a creature its controller controls")
    void sacrificeAbilityCanBoostOwnCreature() {
        harness.addToBattlefield(player1, new FiresOfYavimaya());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YavimayaBarbarian());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FiresOfYavimaya());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new FiresOfYavimaya());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
