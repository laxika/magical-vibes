package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.cards.p.PrimalHuntbeast;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamesOfTheFirebrand.class, DeadlyRecluse.class, ChandraTheFirebrand.class,
        PrimalHuntbeast.class, Unsummon.class})
class FlamesOfTheFirebrandTest extends BaseCardTest {

    @Test
    @DisplayName("Deals all 3 damage to a single target")
    void dealsAllDamageToOneTarget() {
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 3));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Divides damage between a creature and a player")
    void dividesDamageBetweenCreatureAndPlayer() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player2, new DeadlyRecluse());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(recluse.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Deadly Recluse");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Divides 1 damage each among three targets")
    void dividesDamageAmongThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DeadlyRecluse());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DeadlyRecluse());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0,
                Map.of(first.getId(), 1, second.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Assignments must sum to exactly 3")
    void assignmentsMustSumToThree() {
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsTooLittleAssignedDamage() {
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoTargets() {
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.<UUID, Integer>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsZeroDamageAssignedToATarget() {
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNegativeDamageEvenWhenTotalIsThree() {
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(player1.getId(), -1, player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDamageItsControllerAndTheirCreature() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(recluse.getId(), 2, player1.getId(), 1));
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Deadly Recluse");
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsDamageDirectlyToAPlaneswalker() {
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(chandra.getId(), 3));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra, the Firebrand");
        harness.assertInGraveyard(player2, "Chandra, the Firebrand");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetAnOpponentsHexproofCreature() {
        Permanent huntbeast = harness.addToBattlefieldAndReturn(player2, new PrimalHuntbeast());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(huntbeast.getId(), 2, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItsControllersHexproofCreature() {
        Permanent huntbeast = harness.addToBattlefieldAndReturn(player1, new PrimalHuntbeast());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(huntbeast.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Primal Huntbeast");
        assertThat(huntbeast.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNotRedistributeDamageFromATargetThatLeavesTheBattlefield() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player2, new DeadlyRecluse());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, Map.of(recluse.getId(), 2, player2.getId(), 1));
        harness.castInstant(player2, 0, recluse.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Deadly Recluse");
        harness.assertInGraveyard(player1, "Flames of the Firebrand");
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetLeavesTheBattlefield() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player2, new DeadlyRecluse());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, Map.of(recluse.getId(), 3));
        harness.castInstant(player2, 0, recluse.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Deadly Recluse");
        harness.assertInGraveyard(player1, "Flames of the Firebrand");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
