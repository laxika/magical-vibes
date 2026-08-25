package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CongregationGryff.class, HillGiant.class})
class CongregationGryffTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 3 taps creatures with total power 3 and saddles Congregation Gryff")
    void saddleThreeTapsAThreePowerCreature() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        Permanent saddler = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gryff.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Saddled attack boosts Congregation Gryff by the number of Mounts controlled")
    void saddledAttackScalesWithControlledMounts() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        addCreatureReady(player1, new CongregationGryff());
        gryff.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(6);
    }

    @Test
    @DisplayName("Unsaddled attack does not boost Congregation Gryff")
    void unsaddledAttackDoesNotBoost() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(4);
    }
}
