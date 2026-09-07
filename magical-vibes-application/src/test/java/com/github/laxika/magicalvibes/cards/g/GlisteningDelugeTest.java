package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlisteningDeluge.class, GrizzlyBears.class, YouthfulKnight.class, HillGiant.class, Forest.class})
class GlisteningDelugeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives green and white creatures an additional -2/-2")
    void givesAdditionalDebuffToGreenAndWhiteCreatures() {
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent white = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        Permanent red = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        castDeluge();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(green);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(white);
        assertThat(red.getEffectivePower()).isEqualTo(2);
        assertThat(red.getEffectiveToughness()).isEqualTo(2);
        assertThat(land.getEffectivePower()).isEqualTo(0);
        assertThat(land.getEffectiveToughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debuffs expire at end of turn")
    void debuffsExpireAtEndOfTurn() {
        Permanent red = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        castDeluge();
        assertThat(red.getEffectivePower()).isEqualTo(2);
        assertThat(red.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(red.getEffectivePower()).isEqualTo(3);
        assertThat(red.getEffectiveToughness()).isEqualTo(3);
    }

    private void castDeluge() {
        harness.setHand(player1, List.of(new GlisteningDeluge()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
