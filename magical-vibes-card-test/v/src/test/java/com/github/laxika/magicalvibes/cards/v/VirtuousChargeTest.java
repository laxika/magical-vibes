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

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new VolunteerMilitia());
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
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

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost a creature entering after resolution")
    void doesNotBoostCreatureEnteringAfterResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple charges give cumulative boosts")
    void multipleChargesGiveCumulativeBoosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.setHand(player1, List.of(new VirtuousCharge(), new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost includes creatures entering before resolution")
    void boostsCreaturesEnteringBeforeResolution() {
        harness.setHand(player1, List.of(new VirtuousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
}
