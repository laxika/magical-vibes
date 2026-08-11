package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirehoofCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+0 and trample until end of turn")
    void resolvingAbilityBoostsSelfAndGrantsTrample() {
        Permanent cavalry = addReadyCavalry();
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cavalry.getPowerModifier()).isEqualTo(2);
        assertThat(cavalry.getToughnessModifier()).isEqualTo(0);
        assertThat(cavalry.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        Permanent cavalry = addReadyCavalry();
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cavalry.getPowerModifier()).isEqualTo(0);
        assertThat(cavalry.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyCavalry() {
        Permanent cavalry = new Permanent(new FirehoofCavalry());
        cavalry.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cavalry);
        return cavalry;
    }
}
