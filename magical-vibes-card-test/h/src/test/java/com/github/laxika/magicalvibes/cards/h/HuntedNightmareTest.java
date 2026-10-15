package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Crystacean;
import com.github.laxika.magicalvibes.cards.m.MosscoatGoriak;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntedNightmare.class, MosscoatGoriak.class, Crystacean.class})
class HuntedNightmareTest extends BaseCardTest {

    @Test
    void targetOpponentChoosesCreatureForDeathtouchCounter() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MosscoatGoriak());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new MosscoatGoriak());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new Crystacean());
        castHuntedNightmare(player2.getId());

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(bears.getId(), spider.getId());
        assertThat(choice.validIds()).doesNotContain(ownCreature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(spider.getId()));

        assertThat(spider.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void targetOpponentWithOneCreatureGetsCounterWithoutChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MosscoatGoriak());
        castHuntedNightmare(player2.getId());

        GameData gd = harness.getGameData();
        assertThat(creature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void targetOpponentWithNoCreaturesPutsNoCounter() {
        castHuntedNightmare(player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new HuntedNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureEnteringBeforeTriggerResolvesCanReceiveCounter() {
        harness.setHand(player1, List.of(new HuntedNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MosscoatGoriak());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
    }

    @Test
    void creatureWithDeathtouchCounterCanReceiveAnother() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MosscoatGoriak());
        creature.setCounterCount(CounterType.DEATHTOUCH, 1);

        castHuntedNightmare(player2.getId());

        assertThat(creature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void opponentCannotDeclineMandatoryCreatureChoice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MosscoatGoriak());
        harness.addToBattlefield(player2, new Crystacean());
        castHuntedNightmare(player2.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));
        assertThat(first.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
    }

    @Test
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new HuntedNightmare());
        addCreatureReady(player2, new MosscoatGoriak());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new HuntedNightmare());
        Permanent first = addCreatureReady(player2, new MosscoatGoriak());
        Permanent second = addCreatureReady(player2, new Crystacean());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private void castHuntedNightmare(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new HuntedNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, List.of(targetPlayerId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
