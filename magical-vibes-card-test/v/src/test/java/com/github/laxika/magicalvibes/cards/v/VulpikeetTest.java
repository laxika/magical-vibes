package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Vulpikeet.class)
class VulpikeetTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating puts a +1/+1 counter on Vulpikeet")
    void mutatingPutsCounterOnIt() {
        Permanent vulpikeet = addCreatureReady(player1, new Vulpikeet());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, vulpikeet, List.of(vulpikeet.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(vulpikeet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void normalCastingDoesNotAddCounters() {
        harness.castFromHand(player1, new Vulpikeet(), "{3}{W}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vulpikeet")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void repeatedMutationsTriggerEveryVulpikeetInTheMergedCreature() {
        Permanent source = addCreatureReady(player1, new Vulpikeet());
        Permanent other = addCreatureReady(player1, new Vulpikeet());
        Permanent opposing = addCreatureReady(player2, new Vulpikeet());

        for (int mutation = 0; mutation < 2; mutation++) {
            harness.setHand(player1, List.of(new Vulpikeet()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.castWithAlternateCost(player1, 0, source.getId());
            resolveAllTriggers();

            assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                    .isEqualTo(mutation == 0 ? 2 : 5);
            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source, other);
            assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }
    }
}
