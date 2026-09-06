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

        assertThat(ownColorless.getEffectivePower()).isEqualTo(1);
        assertThat(ownColorless.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentMulticoloredNonwhite.getEffectivePower()).isEqualTo(1);
        assertThat(opponentMulticoloredNonwhite.getEffectiveToughness()).isEqualTo(1);
        assertThat(white.getEffectivePower()).isEqualTo(1);
        assertThat(white.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroys nonwhite creatures reduced to 0 toughness")
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
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect nonwhite creatures entering after it resolves")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        castHolyLight();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Scarecrow());

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    private void castHolyLight() {
        harness.castFromHand(player1, new HolyLight(), "{2}{W}");
        harness.passBothPriorities();
    }
}
