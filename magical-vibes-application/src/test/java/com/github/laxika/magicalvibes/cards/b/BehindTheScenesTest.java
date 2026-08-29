package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BehindTheScenesTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have skulk")
    void grantsSkulkToOwnCreatures() {
        harness.addToBattlefield(player1, new BehindTheScenes());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.SKULK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.SKULK)).isFalse();
    }

    @Test
    @DisplayName("Activated ability gives your creatures +1/+1 until end of turn")
    void abilityBoostsOwnCreatures() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BehindTheScenes());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(enchantment.getPowerModifier()).isEqualTo(0);
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(0);
        assertThat(opposingCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activated ability wears off at end of turn")
    void abilityBoostWearsOff() {
        harness.addToBattlefield(player1, new BehindTheScenes());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(0);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(0);
    }
}
