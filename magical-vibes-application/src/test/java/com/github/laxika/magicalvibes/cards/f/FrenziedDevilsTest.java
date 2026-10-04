package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.e.EndTheFestivities;
import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedDevils.class, SnarlingWolf.class, EndTheFestivities.class, Abrade.class})
class FrenziedDevilsTest extends BaseCardTest {

    private Permanent addFrenziedDevils() {
        Permanent devils = harness.addToBattlefieldAndReturn(player1, new FrenziedDevils());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return devils;
    }

    @Test
    @DisplayName("Frenzied Devils gets +2/+2 when its controller casts a noncreature spell")
    void noncreatureSpellPumps() {
        Permanent devils = addFrenziedDevils();
        harness.setHand(player1, List.of(new EndTheFestivities()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(5);
    }

    @Test
    @DisplayName("Frenzied Devils does not trigger when its controller casts a creature spell")
    void creatureSpellDoesNotPump() {
        Permanent devils = addFrenziedDevils();
        harness.setHand(player1, List.of(new SnarlingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(3);
    }

    @Test
    @DisplayName("Frenzied Devils's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent devils = addFrenziedDevils();
        harness.setHand(player1, List.of(new EndTheFestivities()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(3);
    }

    @Test
    @DisplayName("Haste allows Frenzied Devils to attack the turn it is cast")
    void canAttackImmediately() {
        harness.setHand(player1, List.of(new FrenziedDevils()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Frenzied Devils").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Each noncreature spell gives a separate cumulative boost")
    void multipleNoncreatureSpellsStackBoosts() {
        Permanent devils = addFrenziedDevils();
        harness.setHand(player1, List.of(new EndTheFestivities(), new EndTheFestivities()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(7);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Frenzied Devils")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent devils = addFrenziedDevils();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new EndTheFestivities()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(3);
    }

    @Test
    @DisplayName("The cast trigger resolves before the instant that triggered it")
    void boostResolvesBeforeInstantDamage() {
        Permanent devils = addFrenziedDevils();
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, devils.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(5);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frenzied Devils");
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(5);
    }
}
