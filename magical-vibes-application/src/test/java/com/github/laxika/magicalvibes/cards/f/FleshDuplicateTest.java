package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaningWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FleshDuplicate.class, GrizzlyBears.class, WaningWurm.class})
class FleshDuplicateTest extends BaseCardTest {

    @Test
    void copyingCreatureWithoutVanishingAddsThreeTimeCountersAndSacrifices() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent duplicate = castAndChoose(target);

        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(3);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicate.getOriginalCard());
    }

    @Test
    void copyingCreatureWithVanishingUsesCopiedVanishing() {
        Permanent target = addCreatureReady(player2, new WaningWurm());
        target.setCounterCount(CounterType.TIME, 2);

        Permanent duplicate = castAndChoose(target);

        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(duplicate.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(duplicate);
    }

    private Permanent castAndChoose(Permanent target) {
        FleshDuplicate card = new FleshDuplicate();
        harness.castFromHand(player1, card, "{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
