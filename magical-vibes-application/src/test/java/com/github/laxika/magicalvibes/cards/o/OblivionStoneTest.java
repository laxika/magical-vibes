package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OblivionStone.class, AlphaMyr.class, Bonesplitter.class, Forest.class, TrollAscetic.class})
class OblivionStoneTest extends BaseCardTest {

    @Test
    void putsFateCounterOnTargetPermanent() {
        Permanent stone = addCreatureReady(player1, new OblivionStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FATE)).isEqualTo(1);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    void putsFateCounterOnTargetLand() {
        addCreatureReady(player1, new OblivionStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FATE)).isEqualTo(1);
    }

    @Test
    void destroysUnmarkedNonlandsAndClearsFateCountersAfterward() {
        addCreatureReady(player1, new OblivionStone());
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        markedCreature.setCounterCount(CounterType.FATE, 1);
        markedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent indestructibleCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        indestructibleCreature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent unmarkedCreature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent markedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        markedLand.setCounterCount(CounterType.FATE, 1);
        Permanent unmarkedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent unmarkedArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(markedCreature, indestructibleCreature)
                .doesNotContain(unmarkedArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(markedLand, unmarkedLand)
                .doesNotContain(unmarkedCreature);
        harness.assertInGraveyard(player2, "Alpha Myr");
        harness.assertInGraveyard(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Oblivion Stone");
        assertThat(markedCreature.getCounterCount(CounterType.FATE)).isZero();
        assertThat(markedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(markedLand.getCounterCount(CounterType.FATE)).isZero();
        assertThat(unmarkedLand.getCounterCount(CounterType.FATE)).isZero();
    }

    @Test
    void canTargetAPlayerOnlyAsAGroupTargetIsRejected() {
        addCreatureReady(player1, new OblivionStone());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPutAnotherFateCounterOnItself() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new OblivionStone());
        stone.setCounterCount(CounterType.FATE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, stone.getId());
        harness.passBothPriorities();

        assertThat(stone.getCounterCount(CounterType.FATE)).isEqualTo(3);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    void sacrificesMarkedStoneImmediatelyBeforeDestroyingAnything() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new OblivionStone());
        stone.setCounterCount(CounterType.FATE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Oblivion Stone");
        harness.assertInGraveyard(player1, "Oblivion Stone");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInGraveyard(player2, "Alpha Myr");
    }

    @Test
    void clearsEveryFateCounterEvenWhenNoPermanentIsDestroyed() {
        harness.addToBattlefield(player1, new OblivionStone());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        artifact.setCounterCount(CounterType.FATE, 3);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.FATE, 2);
        land.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(artifact.getCounterCount(CounterType.FATE)).isZero();
        assertThat(land.getCounterCount(CounterType.FATE)).isZero();
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void allowsUnmarkedCreatureToRegenerateThroughTheWipe() {
        harness.addToBattlefield(player1, new OblivionStone());
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new TrollAscetic());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(troll);
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Oblivion Stone");
    }
}
