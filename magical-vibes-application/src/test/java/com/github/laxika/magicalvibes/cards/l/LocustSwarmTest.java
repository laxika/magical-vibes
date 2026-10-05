package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Flare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LocustSwarm.class, Flare.class})
class LocustSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("{G} regeneration ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent swarm = addCreatureReady(player1, new LocustSwarm());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(swarm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration ability can be activated more than once each turn")
    void regenerationAbilityRepeatable() {
        Permanent swarm = addCreatureReady(player1, new LocustSwarm());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(swarm.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("{G} untap ability untaps it")
    void untapAbilityUntaps() {
        Permanent swarm = addCreatureReady(player1, new LocustSwarm());
        swarm.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(swarm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability can be activated only once each turn")
    void untapAbilityOncePerTurn() {
        addCreatureReady(player1, new LocustSwarm());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Untap ability can be activated again on a later turn")
    void untapAbilityCanBeActivatedAgainOnLaterTurn() {
        Permanent swarm = addCreatureReady(player1, new LocustSwarm());
        swarm.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        swarm.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(swarm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate an ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new LocustSwarm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void regenerationPreventsLethalDamageAndUntapRemainsAvailable() {
        Permanent swarm = addCreatureReady(player1, new LocustSwarm());
        harness.setHand(player1, List.of(new Flare()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(swarm.isTapped()).isFalse();
        harness.castAndResolveInstant(player1, 0, swarm.getId());

        harness.assertOnBattlefield(player1, "Locust Swarm");
        harness.assertNotInGraveyard(player1, "Locust Swarm");
        assertThat(swarm.isTapped()).isTrue();
        assertThat(swarm.getMarkedDamage()).isZero();
        assertThat(swarm.getRegenerationShield()).isZero();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(swarm.isTapped()).isFalse();
    }

    @Test
    void untapLimitAppliesBeforeAbilityResolves() {
        Permanent swarm = addCreatureReady(player1, new LocustSwarm());
        swarm.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(swarm.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(swarm.isTapped()).isFalse();
    }

    @Test
    void eachSwarmHasItsOwnUntapLimit() {
        Permanent first = addCreatureReady(player1, new LocustSwarm());
        Permanent second = addCreatureReady(player1, new LocustSwarm());
        first.tap();
        second.tap();
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }
}
