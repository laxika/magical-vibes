package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraAblaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldmatesBlessing.class, Shock.class, GrizzlyBears.class, Mountain.class, ChandraAblaze.class})
class ShieldmatesBlessingTest extends BaseCardTest {

    @Test
    void preventsTheNextThreeDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        castBlessing(player2.getId());

        castShock(player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        castShock(player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void preventsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBlessing(target.getId());

        castShock(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void cannotTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ShieldmatesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preventsDamageToTargetPlaneswalkerAndConsumesShield() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraAblaze());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        castBlessing(chandra.getId());

        castShock(chandra.getId());
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        castShock(chandra.getId());
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        castShock(chandra.getId());
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void creatureShieldIsConsumedAcrossDamageEvents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBlessing(target.getId());

        castShock(target.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        castShock(target.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        castShock(target.getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void multipleBlessingsProvideAdditiveShields() {
        harness.setLife(player2, 20);
        castBlessing(player2.getId());
        castBlessing(player2.getId());

        castShock(player2.getId());
        castShock(player2.getId());
        castShock(player2.getId());
        harness.assertLife(player2, 20);

        castShock(player2.getId());
        harness.assertLife(player2, 18);
    }

    @Test
    void shieldingOnePlayerDoesNotProtectAnother() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castBlessing(player1.getId());

        castShock(player2.getId());
        harness.assertLife(player2, 18);

        castShock(player1.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    void unusedPlayerAndCreatureShieldsExpireAtEndOfTurn() {
        harness.setLife(player2, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBlessing(player2.getId());
        castBlessing(target.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        castShock(player2.getId());
        harness.assertLife(player2, 18);

        castShock(target.getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void preventsOnlyThreeCombatDamageAndLeavesExcessDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        castBlessing(player2.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);

        castShock(player2.getId());
        harness.assertLife(player2, 17);
    }

    private void castBlessing(UUID targetId) {
        harness.setHand(player1, List.of(new ShieldmatesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void castShock(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
