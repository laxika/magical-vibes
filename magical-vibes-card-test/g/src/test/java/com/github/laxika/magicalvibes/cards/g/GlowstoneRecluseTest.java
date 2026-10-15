package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlowstoneRecluse.class})
class GlowstoneRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating puts two +1/+1 counters on Glowstone Recluse")
    void mutatingPutsTwoCountersOnIt() {
        Permanent recluse = addCreatureReady(player1, new GlowstoneRecluse());

        mutate(recluse);

        assertThat(recluse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each mutation puts two more +1/+1 counters on Glowstone Recluse")
    void eachMutationPutsTwoMoreCountersOnIt() {
        Permanent recluse = addCreatureReady(player1, new GlowstoneRecluse());

        mutate(recluse);
        mutate(recluse);

        assertThat(recluse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting normally does not put mutation counters on the creature")
    void normalCastingDoesNotPutCountersOnIt() {
        harness.castFromHand(player1, new GlowstoneRecluse(), "{2}{G}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Glowstone Recluse")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mutating one Recluse does not put counters on other creatures")
    void mutationOnlyPutsCountersOnItsSource() {
        Permanent recluse = addCreatureReady(player1, new GlowstoneRecluse());
        Permanent other = addCreatureReady(player1, new GlowstoneRecluse());
        Permanent opposing = addCreatureReady(player2, new GlowstoneRecluse());

        mutate(recluse);

        assertThat(recluse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A mutation trigger cannot put counters on a replacement permanent")
    void sourceLeavingBeforeResolutionDoesNotAffectAnotherRecluse() {
        Permanent recluse = addCreatureReady(player1, new GlowstoneRecluse());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, recluse, List.of(recluse.getCard()), player1.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(recluse);
        Permanent replacement = addCreatureReady(player1, new GlowstoneRecluse());

        resolveAllTriggers();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void mutate(Permanent recluse) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, recluse, List.of(recluse.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
