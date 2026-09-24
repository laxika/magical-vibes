package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinPiledriver;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirdropCondor.class, GoblinPiledriver.class, GlorySeeker.class, Island.class})
class AirdropCondorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a Goblin and deals damage equal to its power to a player")
    void sacrificesGoblinAndDealsItsPowerToPlayer() {
        Permanent condor = addCreatureReady(player1, new AirdropCondor());
        Permanent goblin = addCreatureReady(player1, new GoblinPiledriver());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(condor).doesNotContain(goblin);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(goblin.getCard());
    }

    @Test
    @DisplayName("Uses the Goblin's effective power when it is sacrificed")
    void usesEffectiveSacrificedPower() {
        addCreatureReady(player1, new AirdropCondor());
        Permanent goblin = addCreatureReady(player1, new GoblinPiledriver());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can deal the sacrificed Goblin's power to a creature")
    void dealsDamageToCreature() {
        addCreatureReady(player1, new AirdropCondor());
        Permanent goblin = addCreatureReady(player1, new GoblinPiledriver());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Goblin creature")
    void cannotSacrificeNonGoblinCreature() {
        addCreatureReady(player1, new AirdropCondor());
        Permanent nonGoblin = addCreatureReady(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonGoblin);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new AirdropCondor());
        addCreatureReady(player1, new GoblinPiledriver());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
