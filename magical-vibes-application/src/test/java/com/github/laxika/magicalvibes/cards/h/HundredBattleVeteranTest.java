package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hundred-Battle Veteran")
class HundredBattleVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("gets +2/+4 with three different counter kinds among controlled creatures")
    void getsBoostWithThreeDifferentCounterKinds() {
        Permanent veteran = addCreatureReady(player1, new HundredBattleVeteran());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.LORE, 1);

        assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(2);

        veteran.setCounterCount(CounterType.FINALITY, 1);

        assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(6);
    }

    @Test
    @DisplayName("counts only counter kinds on creatures controlled by its controller")
    void ignoresNoncreaturesAndOpponents() {
        Permanent veteran = addCreatureReady(player1, new HundredBattleVeteran());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.LORE, 1);

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        artifact.setCounterCount(CounterType.FINALITY, 1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.FINALITY, 1);

        assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(2);
    }

    @Test
    @DisplayName("can be cast from the graveyard and enters with a finality counter")
    void castsFromGraveyardWithFinalityCounter() {
        harness.setGraveyard(player1, List.of(new HundredBattleVeteran()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent veteran = findPermanent(player1, "Hundred-Battle Veteran");
        assertThat(veteran.getCounterCount(CounterType.FINALITY)).isEqualTo(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, veteran));

        harness.assertNotOnBattlefield(player1, "Hundred-Battle Veteran");
        harness.assertNotInGraveyard(player1, "Hundred-Battle Veteran");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hundred-Battle Veteran"));
    }
}
