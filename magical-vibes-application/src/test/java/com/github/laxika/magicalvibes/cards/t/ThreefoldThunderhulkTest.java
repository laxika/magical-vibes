package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThreefoldThunderhulk.class, Spellbook.class})
class ThreefoldThunderhulkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three counters and creates Gnomes equal to its power")
    void entersWithCountersAndCreatesGnomes() {
        harness.setHand(player1, List.of(new ThreefoldThunderhulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hulk = findPermanent(player1, "Threefold Thunderhulk");
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player1, "Gnome")).hasSize(3);
    }

    @Test
    @DisplayName("Attack trigger creates Gnomes equal to current power")
    void attackCreatesGnomesEqualToCurrentPower() {
        Permanent hulk = addReadyHulk(player1, 4);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gnome")).hasSize(4);
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing another artifact puts a +1/+1 counter on the Hulk")
    void sacrificeArtifactAddsCounter() {
        Permanent hulk = addReadyHulk(player1, 3);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Cannot sacrifice the Hulk itself when no other artifact is available")
    void cannotActivateWithoutAnotherArtifact() {
        addReadyHulk(player1, 3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact");
    }

    @Test
    void cannotSacrificeOpponentsArtifact() {
        Permanent hulk = addReadyHulk(player1, 3);
        Permanent opposingHulk = addReadyHulk(player2, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact");

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingHulk);
    }

    @Test
    void enterTriggerUsesPowerAtResolution() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new ThreefoldThunderhulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hulk = findPermanent(player1, "Threefold Thunderhulk");
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanents(player1, "Gnome")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gnome")).hasSize(4);
    }

    @Test
    void attackTriggerUsesPowerAtResolution() {
        Permanent hulk = addReadyHulk(player1, 3);
        addReadyHulk(player1, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(List.of(0));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanents(player1, "Gnome")).hasSize(4);
        assertThat(findPermanents(player1, "Gnome")).allSatisfy(gnome -> {
            assertThat(gnome.isTapped()).isFalse();
            assertThat(gnome.isAttacking()).isFalse();
        });
    }

    @Test
    void attackTriggerUsesLastKnownPowerAfterSourceIsSacrificed() {
        Permanent attacker = addReadyHulk(player1, 3);
        Permanent survivor = addReadyHulk(player1, 3);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        declareAttackers(List.of(0));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Spellbook").getId());
        harness.passBothPriorities();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanents(player1, "Gnome")).hasSize(4);
    }

    @Test
    void canSacrificeGnomeTokenWhileSummoningSick() {
        harness.setHand(player1, List.of(new ThreefoldThunderhulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent hulk = findPermanent(player1, "Threefold Thunderhulk");
        Permanent gnome = findPermanent(player1, "Gnome");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gnome.getId());
        assertThat(findPermanents(player1, "Gnome")).hasSize(2);
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private Permanent addReadyHulk(Player player, int counters) {
        Permanent hulk = addCreatureReady(player, new ThreefoldThunderhulk());
        hulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return hulk;
    }
}
