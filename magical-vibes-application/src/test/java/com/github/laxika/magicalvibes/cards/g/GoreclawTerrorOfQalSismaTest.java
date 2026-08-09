package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoreclawTerrorOfQalSismaTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells with power 4 or greater cost {2} less")
    void reducesHighPowerCreatureSpells() {
        harness.addToBattlefield(player1, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Air Elemental");
    }

    @Test
    @DisplayName("Creature spells with power less than 4 are not reduced")
    void doesNotReduceLowPowerCreatureSpells() {
        harness.addToBattlefield(player1, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking boosts and grants trample to qualifying creatures you control")
    void attackBoostsQualifyingCreatures() {
        Permanent goreclaw = addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent airElemental = addCreatureReady(player1, new AirElemental());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemy = addCreatureReady(player2, new AirElemental());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, goreclaw)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goreclaw)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, goreclaw, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enemy, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Attack boost and trample wear off at end of turn")
    void attackBoostAndTrampleWearOffAtEndOfTurn() {
        addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent airElemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.TRAMPLE)).isFalse();
    }
}
