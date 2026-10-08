package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AradaraExpress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindsOfQalSisma.class, AirElemental.class, GrizzlyBears.class, AradaraExpress.class})
class WindsOfQalSismaTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage without ferocious")
    void preventsAllCombatDamageWithoutFerocious() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castWinds();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ferocious allows creatures you control to deal combat damage")
    void ferociousAllowsYourCreaturesToDealCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new AirElemental());
        castWinds();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ferocious prevents combat damage from creatures opponents control")
    void ferociousPreventsOpponentsCombatDamage() {
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new GrizzlyBears());
        castWinds();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Without ferocious both attackers and blockers deal no combat damage")
    void withoutFerociousPreventsDamageToBothCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castWinds();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Gaining ferocious after resolution does not change prevention")
    void gainingFerociousAfterResolutionDoesNotChangePrevention() {
        addCreatureReady(player1, new GrizzlyBears());
        castWinds();
        addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An uncrewed Vehicle does not enable ferocious")
    void uncrewedVehicleDoesNotEnableFerocious() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AradaraExpress());
        castWinds();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ferocious prevents blocker damage while allowing attacker damage")
    void ferociousMakesBlockedCombatOneSided() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new GrizzlyBears());
        castWinds();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ferocious is checked on resolution rather than casting")
    void ferociousIsCheckedOnResolution() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent qualifyingCreature = addCreatureReady(player1, new AirElemental());
        harness.castFromHand(player1, new WindsOfQalSisma(), "{1}{G}");
        gd.playerBattlefields.get(player1.getId()).remove(qualifyingCreature);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Losing ferocious after resolution does not change prevention")
    void losingFerociousAfterResolutionDoesNotChangePrevention() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent qualifyingCreature = addCreatureReady(player1, new AirElemental());
        castWinds();
        gd.playerBattlefields.get(player1.getId()).remove(qualifyingCreature);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    private void castWinds() {
        harness.castFromHand(player1, new WindsOfQalSisma(), "{1}{G}");
        harness.passBothPriorities();
    }
}
