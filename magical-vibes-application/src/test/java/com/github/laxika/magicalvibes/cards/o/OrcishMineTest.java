package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcishMineTest extends BaseCardTest {

    @Test
    @DisplayName("Orcish Mine enters attached to the target land with three ore counters")
    void entersWithThreeOreCounters() {
        Permanent land = castMineOnOpponentLand();

        Permanent aura = findAura();
        assertThat(aura.getAttachedTo()).isEqualTo(land.getId());
        assertThat(aura.getCounterCount(CounterType.ORE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Orcish Mine cannot enchant a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new OrcishMine()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Tapping the enchanted land removes an ore counter")
    void tappingRemovesOreCounter() {
        castMineOnOpponentLand();

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        assertThat(findAura().getCounterCount(CounterType.ORE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Your upkeep removes an ore counter")
    void upkeepRemovesOreCounter() {
        castMineOnOpponentLand();

        advanceToUpkeep(player1);
        resolveStackFully();

        assertThat(findAura().getCounterCount(CounterType.ORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the last ore counter destroys the land and deals 2 damage to its controller")
    void lastCounterDestroysLandAndDamagesController() {
        Permanent land = castMineOnOpponentLand();

        for (int i = 0; i < 3; i++) {
            land.untap();
            harness.tapPermanent(player2, 0);
            resolveStackFully();
        }

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertLife(player2, 18);
    }

    private Permanent castMineOnOpponentLand() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent land = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new OrcishMine()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        return land;
    }

    private Permanent findAura() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Orcish Mine"))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Drives priority until the stack and any deferred mana-ability triggers are fully resolved; tapping
     * a land for mana defers its triggers (CR 603.3) until a player next receives priority.
     */
    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
