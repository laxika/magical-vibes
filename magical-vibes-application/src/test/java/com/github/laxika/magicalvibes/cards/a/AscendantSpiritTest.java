package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AscendantSpiritTest extends BaseCardTest {

    private Permanent addSpirit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new AscendantSpirit());
    }

    private void addSnowMana(int amount) {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.BLUE, amount);
    }

    private void resetPriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Snow mana is required to activate the first ability")
    void requiresSnowMana() {
        addSpirit();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Abilities advance the Spirit through its forms")
    void advancesThroughForms() {
        Permanent spirit = addSpirit();

        addSnowMana(2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);
        assertThat(spirit.getGrantedSubtypes()).contains(CardSubtype.WARRIOR);

        resetPriority();
        addSnowMana(3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, spirit, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        assertThat(spirit.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(spirit.getGrantedSubtypes()).contains(CardSubtype.ANGEL);

        resetPriority();
        addSnowMana(8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        resetPriority();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(8);
    }

    @Test
    @DisplayName("Form abilities do nothing when their prerequisite subtype is missing")
    void prerequisiteSubtypesAreCheckedOnResolution() {
        Permanent spirit = addSpirit();

        addSnowMana(3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(spirit.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);

        resetPriority();
        addSnowMana(4);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Repeated final activations create repeated draw triggers")
    void repeatedFinalActivationsStackDrawTriggers() {
        Permanent spirit = addSpirit();
        spirit.setSummoningSick(false);

        addSnowMana(2 + 3 + 8);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        resetPriority();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        resetPriority();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        resetPriority();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new AscendantSpirit(), new AscendantSpirit()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        spirit.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }
}
