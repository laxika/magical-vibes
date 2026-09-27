package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldWall.class, BarbaryApes.class, Karakas.class})
class ShieldWallTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts all own creatures +0/+2")
    void resolvingBoostsAllOwnCreatures() {
        Permanent firstCreature = addCreatureReady(player1, new BarbaryApes());
        Permanent secondCreature = addCreatureReady(player1, new BarbaryApes());
        harness.castFromHand(player1, new ShieldWall(), "{1}{W}");
        harness.passBothPriorities();

        assertBoostedCreature(firstCreature);
        assertBoostedCreature(secondCreature);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new BarbaryApes());
        Permanent opponentCreature = addCreatureReady(player2, new BarbaryApes());
        harness.castFromHand(player1, new ShieldWall(), "{1}{W}");
        harness.passBothPriorities();

        assertBoostedCreature(ownCreature);
        assertThat(opponentCreature.getPowerModifier()).isEqualTo(0);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost noncreatures you control")
    void doesNotBoostNoncreaturesYouControl() {
        Permanent creature = addCreatureReady(player1, new BarbaryApes());
        Permanent karakas = harness.addToBattlefieldAndReturn(player1, new Karakas());
        harness.castFromHand(player1, new ShieldWall(), "{1}{W}");
        harness.passBothPriorities();

        assertBoostedCreature(creature);
        assertThat(karakas.getPowerModifier()).isEqualTo(0);
        assertThat(karakas.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        Permanent creature = addCreatureReady(player1, new BarbaryApes());
        harness.castFromHand(player1, new ShieldWall(), "{1}{W}");
        harness.passBothPriorities();

        assertBoostedCreature(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Works with an empty battlefield")
    void worksWithEmptyBattlefield() {
        harness.castFromHand(player1, new ShieldWall(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not boost creatures that enter after resolution")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        harness.castFromHand(player1, new ShieldWall(), "{1}{W}");
        harness.passBothPriorities();

        Permanent laterCreature = addCreatureReady(player1, new BarbaryApes());

        assertThat(laterCreature.getPowerModifier()).isEqualTo(0);
        assertThat(laterCreature.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    private void assertBoostedCreature(Permanent creature) {
        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}
