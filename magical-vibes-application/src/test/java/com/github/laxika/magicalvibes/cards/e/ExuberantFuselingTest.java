package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExuberantFuselingTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each oil counter on it")
    void getsPowerForEachOilCounter() {
        Permanent fuseling = harness.addToBattlefieldAndReturn(player1, new ExuberantFuseling());

        assertThat(fuseling.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.getEffectivePower(gd, fuseling)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, fuseling)).isEqualTo(1);

        fuseling.setCounterCount(CounterType.OIL, 3);

        assertThat(gqs.getEffectivePower(gd, fuseling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fuseling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets an oil counter when another creature or artifact you control dies")
    void getsOilCounterWhenOwnCreatureOrArtifactDies() {
        Permanent fuseling = harness.addToBattlefieldAndReturn(player1, new ExuberantFuseling());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());

        putIntoGraveyard(creature);
        assertThat(fuseling.getCounterCount(CounterType.OIL)).isEqualTo(1);

        putIntoGraveyard(artifact);
        assertThat(fuseling.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers for a creature you control even when it is owned by an opponent")
    void triggersForStolenCreatureYouControl() {
        Permanent fuseling = harness.addToBattlefieldAndReturn(player1, new ExuberantFuseling());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(stolenCreature);
        gd.playerBattlefields.get(player1.getId()).add(stolenCreature);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());

        putIntoGraveyard(stolenCreature);

        assertThat(fuseling.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ignores an opponent's permanent and a noncreature nonartifact permanent")
    void ignoresOpponentPermanentAndNoncreatureNonartifact() {
        Permanent fuseling = harness.addToBattlefieldAndReturn(player1, new ExuberantFuseling());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(land);

        assertThat(fuseling.getCounterCount(CounterType.OIL)).isZero();
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
