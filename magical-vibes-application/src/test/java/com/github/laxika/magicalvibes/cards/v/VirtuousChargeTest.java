package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirtuousCharge.class, VolunteerMilitia.class})
class VirtuousChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts all own creatures +1/+1")
    void resolvingBoostsAllOwnCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(firstCreature.getEffectivePower()).isEqualTo(2);
        assertThat(firstCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(secondCreature.getEffectivePower()).isEqualTo(2);
        assertThat(secondCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new VolunteerMilitia());
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostResetsAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost a creature entering after resolution")
    void doesNotBoostCreatureEnteringAfterResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());

        assertThat(existingCreature.getEffectivePower()).isEqualTo(2);
        assertThat(existingCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(laterCreature.getEffectivePower()).isEqualTo(1);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
    }
}
