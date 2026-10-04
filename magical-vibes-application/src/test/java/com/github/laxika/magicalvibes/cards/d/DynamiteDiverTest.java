package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FellFlagship;
import com.github.laxika.magicalvibes.cards.g.GuardianSunmare;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DynamiteDiver.class, FellFlagship.class, GuardianSunmare.class})
class DynamiteDiverTest extends BaseCardTest {

    @Test
    @DisplayName("Its power bonus lets it crew a Vehicle with crew 3")
    void powerBonusLetsItCrewVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FellFlagship());
        Permanent diver = addCreatureReady(player1, new DynamiteDiver());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(vehicle.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(diver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("When it dies, it deals 1 damage to a chosen player")
    void dealsDamageWhenItDies() {
        Permanent diver = addCreatureReady(player1, new DynamiteDiver());
        TestCards.mutableCard(diver).setToughness(0);
        harness.setLife(player2, 20);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Two newly entered Divers can pay saddle 4 with their power bonuses")
    void powerBonusLetsItSaddleMount() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new GuardianSunmare());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DynamiteDiver());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DynamiteDiver());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The crew and saddle power bonus does not increase combat damage")
    void bonusDoesNotIncreaseCombatDamage() {
        addCreatureReady(player1, new DynamiteDiver());
        harness.setLife(player2, 20);

        declareAttackers(java.util.List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The death trigger can deal damage to an opposing creature")
    void deathTriggerDamagesCreature() {
        Permanent diver = addCreatureReady(player1, new DynamiteDiver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuardianSunmare());
        TestCards.mutableCard(diver).setToughness(0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dynamite Diver");
    }
}
