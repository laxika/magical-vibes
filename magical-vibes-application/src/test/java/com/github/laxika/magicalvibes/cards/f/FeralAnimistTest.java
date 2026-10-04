package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.Phytoburst;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeralAnimist.class, Phytoburst.class})
class FeralAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Activating once doubles power and leaves toughness alone")
    void activatingOnceDoublesPower() {
        Permanent animist = addCreatureReady(player1, new FeralAnimist());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Base 2/1, X = 2 → +2/+0 → 4/1.
        assertThat(animist.getEffectivePower()).isEqualTo(4);
        assertThat(animist.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating twice snapshots the boosted power, growing to 8/1")
    void activatingTwiceCompounds() {
        Permanent animist = addCreatureReady(player1, new FeralAnimist());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // After first: 4/1; second X = 4 → +4/+0 → 8/1.
        assertThat(animist.getEffectivePower()).isEqualTo(8);
        assertThat(animist.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Stacked activations use the power at each resolution")
    void stackedActivationsUsePowerAtResolution() {
        Permanent animist = addCreatureReady(player1, new FeralAnimist());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(animist.getEffectivePower()).isEqualTo(8);
        assertThat(animist.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent animist = addCreatureReady(player1, new FeralAnimist());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(animist.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(animist.getEffectivePower()).isEqualTo(2);
        assertThat(animist.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability uses power boosted by another spell at resolution")
    void usesPowerFromOtherBoosts() {
        Permanent animist = addCreatureReady(player1, new FeralAnimist());
        harness.setHand(player1, List.of(new Phytoburst()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, animist.getId());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(animist.getEffectivePower()).isEqualTo(14);
        assertThat(animist.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("A resolved boost does not grow when another spell later increases power")
    void resolvedBoostStaysFixed() {
        Permanent animist = addCreatureReady(player1, new FeralAnimist());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Phytoburst()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, animist.getId());

        assertThat(animist.getEffectivePower()).isEqualTo(9);
        assertThat(animist.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        harness.addToBattlefield(player1, new FeralAnimist());
        Permanent animist = findPermanent(player1, "Feral Animist");
        animist.setSummoningSick(true);
        animist.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(animist.getEffectivePower()).isEqualTo(4);
        assertThat(animist.getEffectiveToughness()).isEqualTo(1);
        assertThat(animist.isTapped()).isTrue();
    }
}
