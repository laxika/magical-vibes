package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.s.Scarecrow;
import com.github.laxika.magicalvibes.cards.s.ScarwoodGoblins;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HolyLight.class, BogRats.class, Scarecrow.class, ScarwoodGoblins.class, Squire.class})
class HolyLightTest extends BaseCardTest {

    @Test
    @DisplayName("Gives nonwhite creatures -1/-1 and leaves white creatures unchanged")
    void debuffsNonwhiteCreatures() {
        Permanent ownColorless = harness.addToBattlefieldAndReturn(player1, new Scarecrow());
        Permanent opponentMulticoloredNonwhite = harness.addToBattlefieldAndReturn(player2, new ScarwoodGoblins());
        Permanent white = harness.addToBattlefieldAndReturn(player2, new Squire());

        castHolyLight();

        assertThat(gqs.getEffectivePower(gd, ownColorless)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownColorless)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentMulticoloredNonwhite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentMulticoloredNonwhite)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts nonwhite creatures reduced to 0 toughness into the graveyard")
    void killsSmallNonwhiteCreatures() {
        harness.addToBattlefield(player2, new BogRats());

        castHolyLight();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Scarecrow());

        castHolyLight();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect nonwhite creatures entering after it resolves")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        castHolyLight();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Scarecrow());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects nonwhite creatures present when the spell resolves")
    void affectsCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new HolyLight(), "{2}{W}");
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Scarecrow());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple casts accumulate and still leave white creatures unchanged")
    void multipleCastsAccumulate() {
        harness.addToBattlefield(player1, new Scarecrow());
        harness.addToBattlefield(player2, new ScarwoodGoblins());
        Permanent white = harness.addToBattlefieldAndReturn(player2, new Squire());

        castHolyLight();
        castHolyLight();

        harness.assertInGraveyard(player1, "Scarecrow");
        harness.assertInGraveyard(player2, "Scarwood Goblins");
        harness.assertOnBattlefield(player2, "Squire");
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
    }

    private void castHolyLight() {
        harness.castFromHand(player1, new HolyLight(), "{2}{W}");
        harness.passBothPriorities();
    }
}
