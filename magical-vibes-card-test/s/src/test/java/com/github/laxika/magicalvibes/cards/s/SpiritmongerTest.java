package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Spiritmonger.class, GrizzlyBears.class, PreyUpon.class})
class SpiritmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself after dealing damage to a creature")
    void putsCounterAfterDealingDamageToCreature() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(monger.getId(), target.getId()));
        resolveAllTriggers();

        assertThat(monger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger from another Spiritmonger dealing damage to a creature")
    void doesNotTriggerFromAnotherSpiritmongerDealingDamage() {
        Permanent watcher = addCreatureReady(player1, new Spiritmonger());
        Permanent attacker = addCreatureReady(player1, new Spiritmonger());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(attacker.getId(), target.getId()));
        resolveAllTriggers();

        assertThat(watcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The black ability grants a regeneration shield")
    void regenerateAbilityGrantsShield() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The green ability changes its color until end of turn")
    void becomesChosenColorUntilEndOfTurn() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, monger)).containsExactly(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, monger)).containsExactly(CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Regeneration saves it from lethal combat damage before its counter resolves")
    void regeneratesFromLethalCombatDamageAndGetsCounter() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        addCreatureReady(player2, new Spiritmonger());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spiritmonger");
        harness.assertNotInGraveyard(player1, "Spiritmonger");
        harness.assertInGraveyard(player2, "Spiritmonger");
        assertThat(monger.getRegenerationShield()).isZero();
        assertThat(monger.isTapped()).isTrue();
        assertThat(monger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lethal damage destroys it before its triggered counter can save it")
    void counterDoesNotSaveItFromLethalDamage() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        Permanent opponent = addCreatureReady(player2, new Spiritmonger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Spiritmonger");
        harness.assertInGraveyard(player2, "Spiritmonger");
        assertThat(monger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage to a player does not give it a counter")
    void playerDamageDoesNotGiveCounter() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(monger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A later color activation replaces the previously chosen color")
    void laterColorChoiceReplacesEarlierColor() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gqs.getEffectiveColors(gd, monger)).containsExactly(CardColor.BLUE);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        assertThat(gqs.getEffectiveColors(gd, monger)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("An unused regeneration shield expires at end of turn")
    void unusedRegenerationShieldExpiresAtEndOfTurn() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(monger.getRegenerationShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(monger.getRegenerationShield()).isZero();
    }
}
