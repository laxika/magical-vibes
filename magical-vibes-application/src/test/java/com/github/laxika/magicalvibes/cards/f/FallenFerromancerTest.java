package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({FallenFerromancer.class, GrizzlyBears.class, LlanowarElves.class})
class FallenFerromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Two generic mana cannot pay the red activation cost")
    void cannotActivateWithoutRedMana() {
        addReadyFerromancer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability gives poison counter to target player (infect)")
    void dealsPoisonCounterToPlayer() {
        harness.setLife(player2, 20);
        addReadyFerromancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Infect: damage to players is dealt as poison counters, not life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability puts -1/-1 counter on target creature (infect)")
    void putsMinusOneCounterOnCreature() {
        addReadyFerromancer(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        // Infect: damage to creatures is dealt as -1/-1 counters
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability kills 1/1 creature with -1/-1 counter (infect)")
    void killsOneOneCreature() {
        addReadyFerromancer(player1);
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyFerromancer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new FallenFerromancer());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent perm = addReadyFerromancer(player1);
        perm.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Activating ability taps the creature and puts ability on stack")
    void activatingTapsAndPutsOnStack() {
        Permanent perm = addReadyFerromancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("One red mana cannot pay the complete activation cost")
    void cannotActivateWithOnlyOneRedMana() {
        addReadyFerromancer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can give its controller a poison counter")
    void canTargetController() {
        harness.setLife(player1, 20);
        addReadyFerromancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(harness.getGameData().playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability retains infect when its source leaves the battlefield")
    void retainsInfectAfterSourceLeaves() {
        harness.setLife(player2, 20);
        Permanent source = addReadyFerromancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.getGameData().playerBattlefields.get(player1.getId()).remove(source);
        harness.getGameData().playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(harness.getGameData().playerPoisonCounters.get(player2.getId())).isEqualTo(1);
    }

    private Permanent addReadyFerromancer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FallenFerromancer());
        perm.setSummoningSick(false);
        return perm;
    }
}
