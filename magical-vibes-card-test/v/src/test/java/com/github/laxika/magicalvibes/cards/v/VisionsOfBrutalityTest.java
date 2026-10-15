package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CinderHellion;
import com.github.laxika.magicalvibes.cards.t.TurnAgainst;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionsOfBrutality.class, GrizzlyBears.class, ZuranSpellcaster.class,
        CinderHellion.class, TurnAgainst.class})
class VisionsOfBrutalityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new VisionsOfBrutality());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature's controller loses life equal to damage dealt")
    void controllerLosesLifeEqualToDamageDealt() {
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        harness.setHand(player1, List.of(new VisionsOfBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, spellcaster.getId());
        harness.passBothPriorities();

        int playerOneLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int playerTwoLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerOneLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(playerTwoLifeBefore - 1);
    }

    @Test
    @DisplayName("Combat damage causes life loss in addition to the damage")
    void combatDamageCausesLifeLoss() {
        Permanent attacker = addCreatureReady(player2, new CinderHellion());
        attachVisions(attacker);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Life loss uses the enchanted creature's controller at resolution")
    void controlChangeBeforeTriggerResolvesChangesWhoLosesLife() {
        Permanent attacker = addCreatureReady(player2, new CinderHellion());
        attachVisions(attacker);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new TurnAgainst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.assertOnBattlefield(player1, "Cinder Hellion");
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage to a creature still causes life loss when both combatants and the Aura die")
    void lethalCreatureCombatStillCausesLifeLoss() {
        Permanent attacker = addCreatureReady(player2, new CinderHellion());
        attachVisions(attacker);
        Permanent blocker = addCreatureReady(player1, new CinderHellion());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.assertNotOnBattlefield(player2, "Cinder Hellion");
        harness.assertNotOnBattlefield(player1, "Cinder Hellion");
        harness.assertNotOnBattlefield(player1, "Visions of Brutality");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Noncombat damage to a creature makes the damage source's controller lose life")
    void noncombatCreatureDamageCausesLifeLoss() {
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player1, new CinderHellion());
        attachVisions(spellcaster);

        harness.activateAbility(player2, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    private void attachVisions(Permanent creature) {
        Permanent aura = new Permanent(new VisionsOfBrutality());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
