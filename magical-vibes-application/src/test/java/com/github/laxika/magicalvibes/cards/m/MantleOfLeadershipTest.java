package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MantleOfLeadership.class, GrizzlyBears.class})
class MantleOfLeadershipTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering under your control gives the enchanted creature +2/+2")
    void boostsEnchantedCreatureWhenCreatureEnters() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMantle(enchantedCreature);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(enchantedCreature.getPowerModifier()).isEqualTo(2);
        assertThat(enchantedCreature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature entering also gives the enchanted creature +2/+2")
    void boostsEnchantedCreatureWhenOpponentsCreatureEnters() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mantle = new Permanent(new MantleOfLeadership());
        mantle.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(mantle);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(enchantedCreature.getPowerModifier()).isEqualTo(2);
        assertThat(enchantedCreature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mantle = new Permanent(new MantleOfLeadership());
        mantle.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(mantle);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(enchantedCreature.getPowerModifier()).isEqualTo(2);
        assertThat(enchantedCreature.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enchantedCreature.getPowerModifier()).isZero();
        assertThat(enchantedCreature.getToughnessModifier()).isZero();
    }

    private void castMantle(Permanent enchantedCreature) {
        harness.setHand(player1, List.of(new MantleOfLeadership()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();
    }
}
