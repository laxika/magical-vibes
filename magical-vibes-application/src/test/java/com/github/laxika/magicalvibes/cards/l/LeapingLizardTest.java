package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LeapingLizard.class)
class LeapingLizardTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{G}: Leaping Lizard gets -0/-1 and gains flying")
    void activationGrantsFlyingAndShrinks() {
        Permanent lizard = addCreatureReady(player1, new LeapingLizard());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated activations stack the toughness reduction")
    void repeatedActivationsStack() {
        Permanent lizard = addCreatureReady(player1, new LeapingLizard());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying and the -0/-1 wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent lizard = addCreatureReady(player1, new LeapingLizard());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lizard, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Lizard can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new LeapingLizard());
        lizard.setSummoningSick(true);
        lizard.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, lizard, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
        assertThat(lizard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating one Lizard does not affect other copies")
    void affectsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new LeapingLizard());
        Permanent friendly = addCreatureReady(player1, new LeapingLizard());
        Permanent opposing = addCreatureReady(player2, new LeapingLizard());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, friendly, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, friendly)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(3);
    }

    @Test
    @DisplayName("Three activations put the Lizard into the graveyard for zero toughness")
    void diesAtZeroToughness() {
        addCreatureReady(player1, new LeapingLizard());
        harness.addMana(player1, ManaColor.GREEN, 6);

        for (int activation = 0; activation < 3; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Leaping Lizard");
        harness.assertInGraveyard(player1, "Leaping Lizard");
    }
}
