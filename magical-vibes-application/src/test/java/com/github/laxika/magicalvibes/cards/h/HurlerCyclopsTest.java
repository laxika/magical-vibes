package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HurlerCyclops.class, GrizzlyBears.class, LlanowarElves.class, Mountain.class})
class HurlerCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature deals 1 damage to a player")
    void dealsDamageToPlayer() {
        Permanent cyclops = addCreatureReady(player1, new HurlerCyclops());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cyclops);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another creature deals 1 damage to a creature")
    void dealsDamageToCreature() {
        addCreatureReady(player1, new HurlerCyclops());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new HurlerCyclops());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new HurlerCyclops());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Mountain")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Cyclops can activate and sacrifices the other Cyclops immediately")
    void activatesWhileSummoningSick() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new HurlerCyclops());
        cyclops.setSummoningSick(true);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HurlerCyclops());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cyclops).doesNotContain(sacrifice);
        harness.assertInGraveyard(player1, "Hurler Cyclops");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cyclops can target itself while sacrificing another creature")
    void canTargetItself() {
        Permanent cyclops = addCreatureReady(player1, new HurlerCyclops());
        harness.addToBattlefield(player1, new HurlerCyclops());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, cyclops.getId());
        harness.passBothPriorities();

        assertThat(cyclops.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cyclops);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new HurlerCyclops());
        harness.addToBattlefield(player2, new HurlerCyclops());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activation requires one mana in addition to the sacrifice")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new HurlerCyclops());
        harness.addToBattlefield(player1, new HurlerCyclops());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
