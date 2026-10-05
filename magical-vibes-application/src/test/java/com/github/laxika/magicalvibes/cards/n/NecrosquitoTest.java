package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necrosquito.class, GrizzlyBears.class, MindStone.class, AuraOfSilence.class,
        Naturalize.class, Shock.class, TreetopVillage.class})
class NecrosquitoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two oil counters and gets +1/+1 for each oil counter")
    void entersWithOilCountersAndScalesWithOilCounters() {
        Permanent necrosquito = addNecrosquito();

        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, necrosquito)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, necrosquito)).isEqualTo(2);

        necrosquito.setCounterCount(CounterType.OIL, 4);

        assertThat(gqs.getEffectivePower(gd, necrosquito)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, necrosquito)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets an oil counter when a creature or artifact you control dies")
    void getsOilCounterWhenOwnCreatureOrArtifactDies() {
        Permanent necrosquito = addNecrosquito();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(3);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature or a noncreature nonartifact permanent")
    void ignoresOpponentCreatureAndNoncreatureNonartifact() {
        Permanent necrosquito = addNecrosquito();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AuraOfSilence());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(2);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    private Permanent addNecrosquito() {
        harness.castFromHand(player1, new Necrosquito(), "{3}{B}");
        harness.passBothPriorities();
        return findPermanent(player1, "Necrosquito");
    }

    @Test
    void triggersForAnimatedLandUsingItsBattlefieldTypes() {
        Permanent necrosquito = addNecrosquito();
        Permanent village = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, village.getId());
            harness.passBothPriorities();
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(village);
        resolveAllTriggers();
        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    void gainsOneCounterForEachSimultaneousDeath() {
        Permanent necrosquito = addNecrosquito();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(necrosquito.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForItsOwnDeath() {
        Permanent necrosquito = addNecrosquito();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, necrosquito.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(necrosquito);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void diesWithNoOilCountersAndOtherCounterTypesDoNotProvideTheBoost() {
        Permanent necrosquito = addNecrosquito();
        necrosquito.setCounterCount(CounterType.OIL, 0);
        necrosquito.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.getEffectivePower(gd, necrosquito)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, necrosquito)).isZero();
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(necrosquito);
    }
}
