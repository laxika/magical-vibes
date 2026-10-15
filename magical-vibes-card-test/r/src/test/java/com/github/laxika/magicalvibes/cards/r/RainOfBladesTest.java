package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RainOfBlades.class, GrizzlyBears.class, FugitiveWizard.class, HowlingMine.class})
class RainOfBladesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each attacking creature")
    void deals1DamageToEachAttackingCreature() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttacker(player1, player2, new GrizzlyBears());
        Permanent a2 = addAttacker(player1, player2, new GrizzlyBears());
        castRainOfBlades(player2);

        assertThat(a1.getMarkedDamage()).isEqualTo(1);
        assertThat(a2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness attacking creatures")
    void killsOneToughnessAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new FugitiveWizard());
        castRainOfBlades(player2);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Does not damage non-attacking creatures")
    void doesNotDamageNonAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new GrizzlyBears());
        Permanent idle = addCreatureReady(player1, new GrizzlyBears());
        castRainOfBlades(player2);

        assertThat(idle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage noncreature permanents")
    void doesNotDamageNoncreatures() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new GrizzlyBears());
        Permanent artifact = addCreatureReady(player1, new HowlingMine());
        castRainOfBlades(player2);

        assertThat(artifact.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Checks which creatures are attacking when it resolves")
    void checksAttackingStatusAtResolution() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new GrizzlyBears());
        castRainOfBladesWithoutResolving(player2);

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private void castRainOfBlades(Player caster) {
        castRainOfBladesWithoutResolving(caster);
        harness.passBothPriorities();
    }

    private void castRainOfBladesWithoutResolving(Player caster) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(caster, new RainOfBlades(), "{W}");
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = addCreatureReady(controller, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    @Test
    @DisplayName("Damages the caster's own attacking creatures")
    void damagesCastersOwnAttackingCreatures() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new GrizzlyBears());

        castRainOfBlades(player1);

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolves without any attacking creatures")
    void resolvesWithoutAttackers() {
        Permanent idle = addCreatureReady(player1, new FugitiveWizard());

        castRainOfBlades(player2);

        assertThat(idle.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Rain of Blades");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two copies accumulate lethal damage on attacking creatures")
    void damageAccumulatesAcrossSpells() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new GrizzlyBears());
        attacker.tap();

        castRainOfBlades(player2);
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);

        castRainOfBlades(player2);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damages blocked attackers but not blocking creatures or players")
    void damagesBlockedAttackersOnly() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castFromHand(player2, new RainOfBlades(), "{W}");

        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
