package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sandstorm.class, BayFalcon.class, GiantMantis.class})
class SandstormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each attacking creature")
    void deals1DamageToEachAttackingCreature() {
        Permanent a1 = addCreatureReady(player1, new GiantMantis());
        Permanent a2 = addCreatureReady(player1, new GiantMantis());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        castSandstorm();

        assertThat(a1.getMarkedDamage()).isEqualTo(1);
        assertThat(a2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness attacking creatures")
    void killsOneToughnessAttackers() {
        addCreatureReady(player1, new BayFalcon());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castSandstorm();

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        harness.assertInGraveyard(player1, "Bay Falcon");
    }

    @Test
    @DisplayName("Does not damage non-attacking creatures")
    void doesNotDamageNonAttackers() {
        addCreatureReady(player1, new BayFalcon());
        Permanent idle = addCreatureReady(player1, new GiantMantis());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        castSandstorm();

        assertThat(idle.getMarkedDamage()).isZero();
    }

    private void castSandstorm() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player2, new Sandstorm(), "{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Also damages attackers controlled by the caster")
    void damagesCastersOwnAttackers() {
        Permanent attacker = addCreatureReady(player1, new GiantMantis());
        Permanent defender = addCreatureReady(player2, new GiantMantis());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        harness.castFromHand(player1, new Sandstorm(), "{G}");
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(defender.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can resolve outside combat with no attacking creatures")
    void resolvesWithoutAttackers() {
        Permanent creature = addCreatureReady(player1, new BayFalcon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new Sandstorm(), "{G}");
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Bay Falcon");
        harness.assertInGraveyard(player1, "Sandstorm");
    }
}
