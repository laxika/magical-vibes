package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthshakerGiant.class, GrizzlyBears.class})
class EarthshakerGiantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives other creatures you control +3/+3 and trample")
    void etbBoostsOtherOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castEarthshakerGiant();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(5);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("ETB boost and trample grant wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castEarthshakerGiant();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The entering Giant is excluded but another Giant is boosted")
    void excludesOnlyTheSourcePermanent() {
        Permanent otherGiant = harness.addToBattlefieldAndReturn(player1, new EarthshakerGiant());
        castEarthshakerGiant();

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(otherGiant.getId()))
                .findFirst().orElseThrow();
        assertThat(otherGiant.getPowerModifier()).isEqualTo(3);
        assertThat(otherGiant.getToughnessModifier()).isEqualTo(3);
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the effects")
    void doesNotAffectLaterCreatures() {
        castEarthshakerGiant();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(laterCreature.getPowerModifier()).isZero();
        assertThat(laterCreature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures present when the trigger resolves receive the effects")
    void determinesRecipientsAtResolution() {
        harness.castFromHand(player1, new EarthshakerGiant(), "{4}{G}{G}");
        harness.passBothPriorities();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The trigger resolves even if the Giant has left the battlefield")
    void resolvesWithoutSourceOnBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new EarthshakerGiant(), "{4}{G}{G}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent != creature);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    private void castEarthshakerGiant() {
        harness.castFromHand(player1, new EarthshakerGiant(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
