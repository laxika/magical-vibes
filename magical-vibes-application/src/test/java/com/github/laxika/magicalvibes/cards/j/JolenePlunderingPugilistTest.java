package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JolenePlunderingPugilist.class, AirElemental.class, GrizzlyBears.class})
class JolenePlunderingPugilistTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure when at least one creature with power 4 or greater attacks")
    void createsOneTreasureForQualifyingAttack() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when no qualifying creature attacks")
    void doesNotCreateTreasureForUnderpoweredAttack() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Does not trigger for a qualifying creature controlled by an opponent")
    void doesNotTriggerForOpponentsCreature() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new AirElemental());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Sacrifices a Treasure and deals 1 damage to any target")
    void sacrificesTreasureAndDealsDamage() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
