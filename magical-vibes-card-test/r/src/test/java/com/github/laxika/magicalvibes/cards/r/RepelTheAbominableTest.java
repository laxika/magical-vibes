package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.g.GalvanicBombardment;
import com.github.laxika.magicalvibes.cards.h.HamletCaptain;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThermoAlchemist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RepelTheAbominable.class, Shock.class, FieldCreeper.class,
        GalvanicBombardment.class, ThermoAlchemist.class, HamletCaptain.class, PreyUpon.class})
class RepelTheAbominableTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from non-Human creatures")
    void preventsCombatDamageFromNonHumanCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new FieldCreeper());
        attacker.setSummoningSick(false);
        castRepelTheAbominable();

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent combat damage from Human creatures")
    void doesNotPreventCombatDamageFromHumanCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HamletCaptain());
        attacker.setSummoningSick(false);
        castRepelTheAbominable();

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents damage from non-Human spells")
    void preventsDamageFromNonHumanSpells() {
        harness.setLife(player1, 20);
        castRepelTheAbominable();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Clears non-Human source prevention at end of turn")
    void clearsAtEndOfTurn() {
        castRepelTheAbominable();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllDamageFromNonHumanSources).isFalse();
    }

    @Test
    @DisplayName("Prevents spell damage to creatures controlled by either player")
    void preventsSpellDamageToCreaturesOfEitherPlayer() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new FieldCreeper());
        castRepelTheAbominable();

        harness.setHand(player1, List.of(new GalvanicBombardment(), new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, ownCreature.getId());
        harness.castAndResolveInstant(player1, 0, opposingCreature.getId());

        harness.assertOnBattlefield(player1, "Field Creeper");
        harness.assertOnBattlefield(player2, "Field Creeper");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A Human Shaman's activated ability still deals damage")
    void humanActivatedAbilityStillDealsDamage() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new ThermoAlchemist());
        human.setSummoningSick(false);
        castRepelTheAbominable();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Prevents damage from a non-Human source entering after resolution")
    void preventsDamageFromSourceEnteringLater() {
        castRepelTheAbominable();
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new FieldCreeper());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Also prevents the caster's non-Human combat damage to the opponent")
    void preventsCastersNonHumanDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());
        attacker.setSummoningSick(false);
        castRepelTheAbominable();
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Non-Human spell damage is dealt normally on the next turn")
    void spellDamageIsNotPreventedNextTurn() {
        castRepelTheAbominable();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Fight damage uses each creature as its source, rather than the sorcery")
    void fightPreventsOnlyNonHumanCreaturesDamage() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player2, new FieldCreeper());
        castRepelTheAbominable();

        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(human.getId(), nonHuman.getId()));

        harness.assertOnBattlefield(player1, "Hamlet Captain");
        assertThat(human.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Field Creeper");
        harness.assertInGraveyard(player2, "Field Creeper");
    }

    private void castRepelTheAbominable() {
        harness.setHand(player1, List.of(new RepelTheAbominable()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
