package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.b.BurningProphet;
import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.t.ThunderDrake;
import com.github.laxika.magicalvibes.cards.u.UginsConjurant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JayaVeneratedFiremage.class, Shock.class, BurningProphet.class,
        ChandrasPyrohelix.class, ThunderDrake.class, UginsConjurant.class})
class JayaVeneratedFiremageTest extends BaseCardTest {

    @Test
    @DisplayName("Jaya's -2 deals 2 damage to any target and removes two loyalty")
    void minusTwoDealsTwoDamage() {
        Permanent jaya = addReadyJaya(player1, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Jaya increases damage from another red source you control")
    void boostsAnotherRedSource() {
        addReadyJaya(player1, 4);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void minusTwoCanDamageCreatureWithoutBoostingItself() {
        addReadyJaya(player1, 4);
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new ThunderDrake());

        harness.activateAbility(player1, 0, 0, null, drake.getId());
        harness.passBothPriorities();

        assertThat(drake.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Thunder Drake");
    }

    @Test
    void minusTwoCanTargetAnotherPlaneswalker() {
        addReadyJaya(player1, 4);
        Permanent opposingJaya = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        opposingJaya.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 0, null, opposingJaya.getId());
        harness.passBothPriorities();

        assertThat(opposingJaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void minusTwoStillResolvesAfterLastLoyaltyIsPaid() {
        addReadyJaya(player1, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Jaya, Venerated Firemage");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void boostsEachRecipientOfDividedDamage() {
        addReadyJaya(player1, 4);
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new ThunderDrake());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, Map.of(drake.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(drake.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 18);
    }

    @Test
    void boostsDamageToItsControllerToo() {
        addReadyJaya(player1, 4);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, Map.of(player1.getId(), 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    void doesNotBoostOpponentsRedSpell() {
        addReadyJaya(player1, 4);
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, Map.of(player1.getId(), 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void boostsRedCombatDamage() {
        addReadyJaya(player1, 4);
        addCreatureReady(player1, new BurningProphet());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void doesNotBoostNonredCombatDamage() {
        addReadyJaya(player1, 4);
        addCreatureReady(player1, new ThunderDrake());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void affectedControllerCanChoosePreventionBeforeDamageBonus() {
        addReadyJaya(player1, 4);
        Permanent conjurant = harness.addToBattlefieldAndReturn(player2, new UginsConjurant());
        conjurant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, Map.of(conjurant.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(conjurant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private Permanent addReadyJaya(Player player, int loyalty) {
        Permanent jaya = harness.addToBattlefieldAndReturn(player, new JayaVeneratedFiremage());
        jaya.setCounterCount(CounterType.LOYALTY, loyalty);
        jaya.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jaya;
    }
}
