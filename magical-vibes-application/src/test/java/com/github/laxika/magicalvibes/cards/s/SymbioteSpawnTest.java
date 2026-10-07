package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SymbioteSpawn.class, GrizzlyBears.class, Shock.class})
class SymbioteSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("When Symbiote Spawn dies, each opponent loses 2 life and you gain 2 life")
    void selfDeathDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent symbioteSpawn = harness.addToBattlefieldAndReturn(player1, new SymbioteSpawn());
        killWithShock(player1, symbioteSpawn);

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Another creature dying does not trigger Symbiote Spawn")
    void anotherCreatureDeathDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new SymbioteSpawn());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(player1, creature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Death benefits the last controller rather than the owner")
    void deathBenefitsControllerRatherThanOwner() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        SymbioteSpawn card = new SymbioteSpawn();
        card.setOwnerId(player1.getId());
        Permanent spawn = harness.addToBattlefieldAndReturn(player2, card);

        killWithShock(player1, spawn);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player1, "Symbiote Spawn");
        harness.assertNotInGraveyard(player2, "Symbiote Spawn");
    }

    @Test
    @DisplayName("Each simultaneously dying copy triggers once")
    void simultaneousDeathsEachTriggerOnce() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SymbioteSpawn());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SymbioteSpawn());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof SymbioteSpawn).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling Symbiote Spawn does not trigger its death ability")
    void exileDoesNotTriggerDeathAbility() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent spawn = harness.addToBattlefieldAndReturn(player1, new SymbioteSpawn());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, spawn));

        harness.assertNotOnBattlefield(player1, "Symbiote Spawn");
        harness.assertNotInGraveyard(player1, "Symbiote Spawn");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
