package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduAscendancy.class, AlpineGrizzly.class})
class MarduAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature attack creates a tapped and attacking Goblin")
    void nontokenCreatureAttackCreatesGoblin() {
        addReadyAscendancy();
        addCreatureReady(player1, new AlpineGrizzly());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
        });

        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.getCard().isToken()).isTrue();
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.isAttacking()).isTrue();
        assertThat(goblin.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(goblin.isAttackedThisTurn()).isFalse();
        assertThat(goblin.getEffectivePower()).isEqualTo(1);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A token creature attack does not trigger Mardu Ascendancy")
    void tokenCreatureAttackDoesNotTrigger() {
        addReadyAscendancy();
        addCreatureReady(player1, new AlpineGrizzly());
        declareAttackers(List.of(1));
        resolveAllTriggers();
        Permanent goblin = findPermanent(player1, "Goblin");

        harness.performUntapStep(player1);
        goblin.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(2)));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Goblin")).containsExactly(goblin);
    }

    @Test
    @DisplayName("Sacrificing Mardu Ascendancy gives your creatures +0/+3 until end of turn")
    void sacrificeBoostsOwnCreatures() {
        addReadyAscendancy();
        Permanent bear = addCreatureReady(player1, new AlpineGrizzly());
        Permanent enemy = addCreatureReady(player2, new AlpineGrizzly());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mardu Ascendancy");
        harness.assertInGraveyard(player1, "Mardu Ascendancy");
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);
        assertThat(enemy.getEffectivePower()).isEqualTo(4);
        assertThat(enemy.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each nontoken attacker triggers each Mardu Ascendancy separately")
    void multipleAttackersAndAscendanciesCreateOneTokenPerTrigger() {
        addReadyAscendancy();
        addReadyAscendancy();
        addCreatureReady(player1, new AlpineGrizzly());
        addCreatureReady(player1, new AlpineGrizzly());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(2, 3));
            assertThat(gd.stack).hasSize(4);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Goblin")).hasSize(4)
                .allSatisfy(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
                });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's attacker does not trigger your Mardu Ascendancy")
    void opponentAttackDoesNotTrigger() {
        addReadyAscendancy();
        addCreatureReady(player2, new AlpineGrizzly());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("An attack trigger still creates a Goblin after Ascendancy is sacrificed")
    void attackTriggerSurvivesSacrificeAndLaterTokenIsNotBoosted() {
        addReadyAscendancy();
        Permanent bear = addCreatureReady(player1, new AlpineGrizzly());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            harness.activateAbility(player1, 0, null, null);

            harness.assertNotOnBattlefield(player1, "Mardu Ascendancy");
            harness.assertInGraveyard(player1, "Mardu Ascendancy");
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
            assertThat(gd.stack).hasSize(2);
            resolveAllTriggers();
        });

        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
        assertThat(goblin.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A Goblin created before sacrifice resolution receives the boost")
    void existingGoblinReceivesBoost() {
        addReadyAscendancy();
        addCreatureReady(player1, new AlpineGrizzly());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
            harness.activateAbility(player1, 0, null, null);
            resolveAllTriggers();
        });

        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.getEffectivePower()).isEqualTo(1);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The sacrifice boost includes creatures entering before resolution and expires at cleanup")
    void boostUsesCreaturesAtResolutionAndExpires() {
        addReadyAscendancy();
        Permanent original = addCreatureReady(player1, new AlpineGrizzly());

        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new AlpineGrizzly());
        resolveAllTriggers();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new AlpineGrizzly());

        assertThat(original.getEffectiveToughness()).isEqualTo(5);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(5);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(original.getEffectiveToughness()).isEqualTo(2);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyAscendancy() {
        return harness.addToBattlefieldAndReturn(player1, new MarduAscendancy());
    }
}
