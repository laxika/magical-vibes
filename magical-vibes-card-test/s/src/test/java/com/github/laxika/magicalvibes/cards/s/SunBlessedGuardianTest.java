package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.f.FurnaceBlessedConqueror;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunBlessedGuardian.class, FurnaceBlessedConqueror.class, ChandraHopesBeacon.class})
class SunBlessedGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("The transform ability can pay its Phyrexian mana with life")
    void transformsByPayingPhyrexianManaWithLife() {
        Permanent guardian = addGuardian();
        int startingLife = gd.getLife(player1.getId());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("The back face creates a tapped and attacking copy carrying the source's counters")
    void attackCreatesTappedAttackingCopyWithSourceCounters() {
        Permanent guardian = addTransformedGuardian();
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attackAndResolveCopy();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack copy is sacrificed at the next end step")
    void attackCopyIsSacrificedAtNextEndStep() {
        Permanent guardian = addTransformedGuardian();
        attackAndResolveCopy();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(guardian);
    }

    @Test
    void transformsByPayingRedManaWithoutLosingLife() {
        Permanent guardian = addGuardian();
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int startingLife = gd.getLife(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(5);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent guardian = addGuardian();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guardian.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent guardian = addGuardian();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guardian.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        addGuardian();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void copyUsesCounterCountWhenTriggerResolves() {
        Permanent guardian = addTransformedGuardian();
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.stack).hasSize(1);
            guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
            harness.passBothPriorities();
        });

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
    }

    @Test
    void copyRetainsBothColorsOfTheBackFace() {
        addTransformedGuardian();
        attackAndResolveCopy();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, token))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
    }

    @Test
    void controllerChoosesWhetherCopyAttacksPlayerOrPlaneswalker() {
        addTransformedGuardian();
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        chandra.setCounterCount(CounterType.LOYALTY, 5);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
        });

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private Permanent addGuardian() {
        return addCreatureReady(player1, new SunBlessedGuardian());
    }

    private Permanent addTransformedGuardian() {
        Permanent guardian = addGuardian();
        guardian.setCard(guardian.getCard().getBackFaceCard());
        guardian.setTransformed(true);
        return guardian;
    }

    private void attackAndResolveCopy() {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.passBothPriorities();
        });
    }
}
