package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObsidianFireheartTest extends BaseCardTest {

    @Test
    @DisplayName("Ability puts a blaze counter on a land and burns its controller during upkeep")
    void putsBlazeCounterAndBurnsLandController() {
        Permanent fireheart = addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(mountain.getCounterCount(CounterType.BLAZE)).isEqualTo(1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(fireheart).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Burning ability persists after Obsidian Fireheart leaves the battlefield")
    void burningAbilityPersistsAfterSourceLeaves() {
        Permanent fireheart = addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, mountain.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(fireheart);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a land that already has a blaze counter")
    void cannotTargetLandWithBlazeCounter() {
        addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.setCounterCount(CounterType.BLAZE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addReadyFireheart(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyFireheart(Player player) {
        return addCreatureReady(player, new ObsidianFireheart());
    }
}
