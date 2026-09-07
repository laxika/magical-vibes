package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishMine.class, Mountain.class, VampireHexmage.class})
class OrcishMineTest extends BaseCardTest {

    @Test
    @DisplayName("Orcish Mine enters attached to the target land with three ore counters")
    void entersWithThreeOreCounters() {
        Permanent land = castMineOnOpponentLand();

        Permanent aura = findPermanent(player1, "Orcish Mine");
        assertThat(aura.getAttachedTo()).isEqualTo(land.getId());
        assertThat(aura.getCounterCount(CounterType.ORE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Orcish Mine cannot enchant a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new VampireHexmage());
        Permanent creature = findPermanent(player2, "Vampire Hexmage");
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

        assertThat(findPermanent(player1, "Orcish Mine").getCounterCount(CounterType.ORE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Your upkeep removes an ore counter")
    void upkeepRemovesOreCounter() {
        castMineOnOpponentLand();

        advanceToUpkeep(player1);
        resolveStackFully();

        assertThat(findPermanent(player1, "Orcish Mine").getCounterCount(CounterType.ORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove an ore counter")
    void opponentsUpkeepDoesNotRemoveOreCounter() {
        castMineOnOpponentLand();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Orcish Mine").getCounterCount(CounterType.ORE)).isEqualTo(3);
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

    @Test
    @DisplayName("Removing all ore counters by another effect destroys the land and damages its controller")
    void lastCounterRemovedByAnotherEffectTriggersPayoff() {
        Permanent land = castMineOnOpponentLand();
        Permanent hexmage = harness.addToBattlefieldAndReturn(player1, new VampireHexmage());
        hexmage.setSummoningSick(false);
        Permanent aura = findPermanent(player1, "Orcish Mine");

        harness.activateAbility(player1, 1, null, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(land.getId()));
        harness.assertNotOnBattlefield(player1, "Orcish Mine");
        harness.assertLife(player2, 18);
    }

    private Permanent castMineOnOpponentLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new OrcishMine()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        return land;
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
