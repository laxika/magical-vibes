package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.d.DuneMover;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExuberantFuseling.class, CopperLonglegs.class, DuneMover.class, PropheticPrism.class, Forest.class})
class ExuberantFuselingTest extends BaseCardTest {

    @Test
    @DisplayName("Gets one oil counter when it enters the battlefield")
    void getsOilCounterOnEntering() {
        Permanent fuseling = harness.enterBattlefieldAndReturn(player1, new ExuberantFuseling());

        harness.passBothPriorities();

        assertThat(fuseling.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, fuseling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for its own death")
    void doesNotTriggerForOwnDeath() {
        Permanent fuseling = harness.addToBattlefieldAndReturn(player1, new ExuberantFuseling());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, fuseling));

        assertThat(gd.stack).isEmpty();
    }

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());

        putIntoGraveyard(creature);
        assertThat(fuseling.getCounterCount(CounterType.OIL)).isEqualTo(1);

        putIntoGraveyard(artifact);
        assertThat(fuseling.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers for a creature you control even when it is owned by an opponent")
    void triggersForStolenCreatureYouControl() {
        Permanent fuseling = harness.addToBattlefieldAndReturn(player1, new ExuberantFuseling());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
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
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(land);

        assertThat(fuseling.getCounterCount(CounterType.OIL)).isZero();
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An artifact creature dying gives only one oil counter")
    void artifactCreatureGivesOneCounter() {
        Permanent fuseling = harness.addToBattlefieldAndReturn(player1, new ExuberantFuseling());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuneMover());

        putIntoGraveyard(creature);

        assertThat(fuseling.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for an animated land dying as a creature")
    void triggersForAnimatedLand() {
        Permanent fuseling = harness.addToBattlefieldAndReturn(player1, new ExuberantFuseling());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setAnimatedUntilEndOfTurn(true);
        land.setAnimatedPower(3);
        land.setAnimatedToughness(3);

        putIntoGraveyard(land);

        assertThat(fuseling.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }
}
