package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoggSentryTest extends BaseCardTest {

    private void opponentCastsSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
    }

    @Test
    @DisplayName("Triggers when an opponent casts a spell")
    void triggersWhenOpponentCastsSpell() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsSpell();

        StackEntry trigger = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst().orElseThrow();
        assertThat(trigger.getCard().getName()).isEqualTo("Mogg Sentry");
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(BoostSelfEffect.class);
    }

    @Test
    @DisplayName("Resolving the trigger gives Mogg Sentry +2/+2 until end of turn")
    void resolvingTriggerBoostsSentry() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsSpell();
        harness.passBothPriorities(); // Resolve Mogg Sentry trigger

        Permanent sentry = sentry();
        assertThat(sentry.getPowerModifier()).isEqualTo(2);
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when the controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new MoggSentry());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsSpell();
        harness.passBothPriorities(); // Resolve Mogg Sentry trigger

        assertThat(sentry().getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent sentry = sentry();
        assertThat(sentry.getPowerModifier()).isEqualTo(0);
        assertThat(sentry.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent sentry() {
        GameData gd = harness.getGameData();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mogg Sentry"))
                .findFirst().orElseThrow();
    }
}
