package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronBully;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DovinHandOfControl.class, AngelsFeather.class, Divination.class, GrizzlyBears.class,
        Shock.class, IronBully.class, JayaVeneratedFiremage.class})
class DovinHandOfControlTest extends BaseCardTest {

    @Test
    void taxesOpponentArtifactSpells() {
        harness.addToBattlefield(player1, new DovinHandOfControl());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new AngelsFeather()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void taxesOpponentInstantSpells() {
        harness.addToBattlefield(player1, new DovinHandOfControl());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void taxesOpponentSorcerySpells() {
        harness.addToBattlefield(player1, new DovinHandOfControl());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTaxControllerSpells() {
        harness.addToBattlefield(player1, new DovinHandOfControl());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTaxCreatureSpells() {
        harness.addToBattlefield(player1, new DovinHandOfControl());
        prepareMainPhase(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void minusOnePreventsDamageToAndByOpponentPermanentUntilNextTurn() {
        harness.setLife(player1, 20);
        Permanent dovin = addReadyDovin(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        target.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void minusOneCannotTargetOwnPermanent() {
        addReadyDovin(player1);
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    void taxesArtifactCreatureSpellsOnlyOnce() {
        harness.addToBattlefield(player1, new DovinHandOfControl());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new IronBully()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void minusOneCanTargetNoncreatureArtifact() {
        Permanent dovin = addReadyDovin(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusOnePreventsNoncombatDamageToPlaneswalker() {
        addReadyDovin(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        target.setCounterCount(CounterType.LOYALTY, 5);
        int loyalty = target.getCounterCount(CounterType.LOYALTY);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyalty);
    }

    @Test
    void minusOnePreventsCombatDamageToPlaneswalker() {
        addReadyDovin(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        target.setCounterCount(CounterType.LOYALTY, 5);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new IronBully());
        int loyalty = target.getCounterCount(CounterType.LOYALTY);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(target.getId());
        resolveCombat(player1);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyalty);
    }

    @Test
    void minusOnePreventsNoncombatDamageDealtByPlaneswalker() {
        addReadyDovin(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        prepareMainPhase(player2);
        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void preventionSurvivesDovinLeavingAndExpiresOnControllersNextTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        Permanent dovin = addReadyDovin(player1);
        dovin.setCounterCount(CounterType.LOYALTY, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dovin);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private Permanent addReadyDovin(Player player) {
        Permanent dovin = harness.addToBattlefieldAndReturn(player, new DovinHandOfControl());
        dovin.setCounterCount(CounterType.LOYALTY, 3);
        dovin.setSummoningSick(false);
        prepareMainPhase(player);
        return dovin;
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
