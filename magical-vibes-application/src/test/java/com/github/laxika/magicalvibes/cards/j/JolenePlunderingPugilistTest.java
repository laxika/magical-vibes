package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.Cactarantula;
import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JolenePlunderingPugilist.class, Cactarantula.class, SterlingHound.class})
class JolenePlunderingPugilistTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure when at least one creature with power 4 or greater attacks")
    void createsOneTreasureForQualifyingAttack() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new Cactarantula());
        addCreatureReady(player1, new SterlingHound());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when no qualifying creature attacks")
    void doesNotCreateTreasureForUnderpoweredAttack() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new SterlingHound());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Does not trigger for a qualifying creature controlled by an opponent")
    void doesNotTriggerForOpponentsCreature() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player2, new Cactarantula());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Sacrifices a Treasure and deals 1 damage to any target")
    void sacrificesTreasureAndDealsDamage() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new Cactarantula());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        int lifeBeforeAbility = gd.playerLifeTotals.get(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBeforeAbility - 1);
    }

    @Test
    void multipleQualifyingAttackersCreateOnlyOneTreasure() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new Cactarantula());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void joleneCanTriggerForHerOwnAttack() {
        addCreatureReady(player1, new JolenePlunderingPugilist());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void attackTriggerResolvesAfterQualifyingAttackerLeaves() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        var attacker = addCreatureReady(player1, new Cactarantula());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutTreasureEvenWithAnotherArtifact() {
        addCreatureReady(player1, new JolenePlunderingPugilist());
        addCreatureReady(player1, new SterlingHound());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Sterling Hound");
        harness.assertLife(player2, 20);
    }

    @Test
    void abilityCanTargetCreatureWhileJoleneIsTapped() {
        var jolene = addCreatureReady(player1, new JolenePlunderingPugilist());
        var target = addCreatureReady(player1, new SterlingHound());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(jolene.isTapped()).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }
}
