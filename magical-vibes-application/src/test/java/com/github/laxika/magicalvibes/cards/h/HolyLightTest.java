package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HolyLight.class, FugitiveWizard.class, GrizzlyBears.class, SavannahLions.class})
class HolyLightTest extends BaseCardTest {

    @Test
    @DisplayName("Gives nonwhite creatures -1/-1 and leaves white creatures unchanged")
    void debuffsNonwhiteCreatures() {
        Permanent ownNonwhite = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentNonwhite = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent white = harness.addToBattlefieldAndReturn(player2, new SavannahLions());

        castHolyLight();

        assertThat(ownNonwhite.getEffectivePower()).isEqualTo(1);
        assertThat(ownNonwhite.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentNonwhite.getEffectivePower()).isEqualTo(1);
        assertThat(opponentNonwhite.getEffectiveToughness()).isEqualTo(1);
        assertThat(white.getEffectivePower()).isEqualTo(2);
        assertThat(white.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys nonwhite creatures reduced to 0 toughness")
    void killsSmallNonwhiteCreatures() {
        harness.addToBattlefield(player2, new FugitiveWizard());

        castHolyLight();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHolyLight();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    private void castHolyLight() {
        harness.setHand(player1, List.of(new HolyLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
