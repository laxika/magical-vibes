package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
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

@CardUsed({LoreholdPledgemage.class, BarkshellBlessing.class, GiantGrowth.class, GrizzlyBears.class})
class LoreholdPledgemageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts Lorehold Pledgemage until end of turn")
    void castingInstantBoostsLoreholdPledgemage() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(pledgemage.getEffectivePower()).isEqualTo(3);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copying an instant boosts Lorehold Pledgemage")
    void copyingInstantBoostsLoreholdPledgemage() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(pledgemage.getEffectivePower()).isEqualTo(4);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Lorehold Pledgemage")
    void castingCreatureDoesNotBoostLoreholdPledgemage() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(pledgemage.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent pledgemage = addCreatureReady(player1, new LoreholdPledgemage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pledgemage.getEffectivePower()).isEqualTo(2);
        assertThat(pledgemage.getEffectiveToughness()).isEqualTo(2);
    }
}
