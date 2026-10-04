package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.a.AngrathTheFlameChained;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrilledDeathspitter.class, Shock.class, FanaticalFirebrand.class, AngrathTheFlameChained.class})
class FrilledDeathspitterTest extends BaseCardTest {

    @Test
    void spellDamageTriggersEnrage() {
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID deathspitterId = harness.getPermanentId(player2, "Frilled Deathspitter");
        harness.castAndResolveInstant(player1, 0, deathspitterId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Frilled Deathspitter");
    }

    @Test
    void eachSeparateDamageEventTriggersEvenWhenTheSecondIsLethal() {
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.addToBattlefield(player1, new FanaticalFirebrand());
        harness.addToBattlefield(player1, new FanaticalFirebrand());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        UUID deathspitterId = harness.getPermanentId(player2, "Frilled Deathspitter");

        harness.activateAbility(player1, 0, null, deathspitterId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Frilled Deathspitter");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);

        harness.activateAbility(player1, 0, null, deathspitterId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Frilled Deathspitter");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void enrageCanTargetEitherPlayersPlaneswalker(boolean ownPlaneswalker) {
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.addToBattlefield(player1, new FanaticalFirebrand());
        var planeswalkerController = ownPlaneswalker ? player2 : player1;
        Permanent angrath = harness.addToBattlefieldAndReturn(planeswalkerController, new AngrathTheFlameChained());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Frilled Deathspitter"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, angrath.getId());
        harness.passBothPriorities();

        assertThat(angrath.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Frilled Deathspitter");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void combatDamageTriggersEnrage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FanaticalFirebrand());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FrilledDeathspitter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Frilled Deathspitter");
        harness.assertInGraveyard(player1, "Fanatical Firebrand");
    }
}
