package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntmasterLiger.class, GrizzlyBears.class})
class HuntmasterLigerTest extends BaseCardTest {

    @Test
    @DisplayName("Mutations boost other creatures by the number of times Huntmaster Liger mutated")
    void mutationsScaleTheBoostAndExcludeTheSource() {
        Permanent liger = addCreatureReady(player1, new HuntmasterLiger());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, liger, List.of(liger.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, liger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, liger)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, liger, List.of(liger.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(5);
    }
}
