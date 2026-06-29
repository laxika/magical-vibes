package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pounce;
import com.github.laxika.magicalvibes.cards.s.SensoryDeprivation;
import com.github.laxika.magicalvibes.cards.s.StrongholdConfessor;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression coverage for the "negative effective power" bug class. A creature with negative
 * effective power (e.g. Stronghold Confessor under Sensory Deprivation) must not be allowed
 * to produce negative damage, either in combat or through "deals damage equal to its power"
 * effects. CR 510.1a: a creature assigns 0 combat damage if its power is 0 or less.
 *
 * <p>Before the fix, a 1/1 attacker reduced to -2/1 would:
 * <ul>
 *   <li>Return -2 from the combat-damage query, making the engine believe it owed damage.</li>
 *   <li>Prompt the controller for manual damage assignment with an impossible target total,
 *       deadlocking the AI (no non-negative distribution satisfies {@code sum == -2}).</li>
 * </ul>
 */
class DamageResolutionNegativePowerTest extends BaseCardTest {

    @Test
    @DisplayName("Double-blocked attacker with negative effective power resolves without asking for manual assignment")
    void negativePowerAttackerDoubleBlocked_resolvesWithoutDeadlock() {
        // 1/1 attacker
        harness.addToBattlefield(player1, new StrongholdConfessor());
        Permanent attacker = findPermanent(player1, "Stronghold Confessor");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        // -3/-0 aura attached: attacker is now effectively -2/1
        Permanent aura = new Permanent(new SensoryDeprivation());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(-2);
        assertThat(gqs.getEffectiveCombatDamage(gd, attacker)).isEqualTo(0);

        // Two 1/1 blockers
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker1 = defBf.get(0);
        Permanent blocker2 = defBf.get(1);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE. If the attacker incorrectly
        // returns negative combat damage, the engine pauses here for manual assignment
        // and the blockers deadlock the AI.
        harness.passBothPriorities();

        // The combat damage step must NOT stall on manual assignment input.
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

        // Both blockers receive 0 damage from the attacker and survive.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Llanowar Elves"))
                .hasSize(2);
        assertThat(blocker1.getMarkedDamage()).isZero();
        assertThat(blocker2.getMarkedDamage()).isZero();

        // Blockers deal lethal back to the 1-toughness attacker (1+1 marked damage).
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Stronghold Confessor"));

        // Defending player took no combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Fight effect: negative-power creature deals 0, its opponent deals its full power")
    void fightWithNegativePowerCreature_noDamageDealtByIt() {
        // 2/2 Grizzly Bears reduced to -1/2 via Sensory Deprivation
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent weakBear = findPermanent(player1, "Grizzly Bears");
        Permanent aura = new Permanent(new SensoryDeprivation());
        aura.setAttachedTo(weakBear.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, weakBear)).isEqualTo(-1);
        assertThat(gqs.getPowerBasedDamage(gd, weakBear)).isZero();

        // 1/1 Llanowar Elves opponent
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent elves = findPermanent(player2, "Llanowar Elves");

        harness.setHand(player1, List.of(new Pounce()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = weakBear.getId();
        UUID elvesId = elves.getId();
        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        // The weak bear deals 0 damage to the elves, so they survive with no marked damage.
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        Permanent survivingElves = findPermanent(player2, "Llanowar Elves");
        assertThat(survivingElves.getMarkedDamage()).isZero();

        // The elves deal their full 1 damage to the bear, which is lethal against toughness 2
        // minus any toughness modifiers. Grizzly Bears is 2/2 base, Sensory Deprivation is -3/-0,
        // so toughness is 2. 1 damage is non-lethal, bear survives with 1 marked damage.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(weakBear.getMarkedDamage()).isEqualTo(1);
    }
}
