package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RubyCollector.class, GrizzlyBears.class})
class RubyCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures Mox Ruby after attacking with at least three creatures")
    void conjuresMoxRubyAfterAttackingWithThreeCreatures() {
        addReadyRubyCollector();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        harness.assertInHand(player1, "Mox Ruby");
    }

    @Test
    @DisplayName("Does not trigger with fewer than three attackers")
    void doesNotTriggerWithFewerThanThreeAttackers() {
        addReadyRubyCollector();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Mox Ruby");
    }

    @Test
    @DisplayName("The conjure trigger works only once for the permanent")
    void conjureTriggersOnlyOnce() {
        addReadyRubyCollector();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();
        int handSizeAfterFirstAttack = gd.playerHands.get(player1.getId()).size();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterFirstAttack);
    }

    @Test
    @DisplayName("Activated ability boosts creatures you control until end of turn")
    void activatedAbilityBoostsOwnCreatures() {
        Permanent rubyCollector = addReadyRubyCollector();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rubyCollector.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(opponent.getPowerModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rubyCollector.getPowerModifier()).isZero();
        assertThat(bears.getPowerModifier()).isZero();
    }

    private Permanent addReadyRubyCollector() {
        return addCreatureReady(player1, new RubyCollector());
    }
}
