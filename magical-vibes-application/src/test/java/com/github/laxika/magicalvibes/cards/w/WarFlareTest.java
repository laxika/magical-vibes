package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarFlare.class, GrizzlyBears.class, Mountain.class})
class WarFlareTest extends BaseCardTest {

    @Test
    void boostsAndUntapsOwnCreaturesOnly() {
        Permanent ownTappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownUntappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownTappedCreature.tap();
        opponentCreature.tap();

        castForMana();

        assertThat(ownTappedCreature.isTapped()).isFalse();
        assertThat(ownUntappedCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownTappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownTappedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownUntappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownUntappedCreature)).isEqualTo(3);
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castForMana();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void doesNotUntapNoncreaturePermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        land.tap();

        castForMana();

        assertThat(land.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "War Flare");
    }

    @Test
    void affectsCreaturesPresentAtResolutionRatherThanCasting() {
        harness.castFromHand(player1, new WarFlare(), "{2}{R}{W}");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void doesNotAffectCreaturesEnteringAfterResolution() {
        castForMana();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.assertInGraveyard(player1, "War Flare");
    }

    private void castForMana() {
        harness.castFromHand(player1, new WarFlare(), "{2}{R}{W}");
        harness.passBothPriorities();
    }
}
