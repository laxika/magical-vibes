package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConduitOfStorms.class})
class ConduitOfStormsTest extends BaseCardTest {

    @Test
    @DisplayName("Multiple attacking Conduits each add mana only in the next main phase")
    void multipleAttackersEachScheduleMana() {
        addCreatureReady(player1, new ConduitOfStorms());
        addCreatureReady(player1, new ConduitOfStorms());

        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Two pending transform activations transform the Conduit only once")
    void multipleTransformActivationsDoNotFlipBack() {
        Permanent conduit = addCreatureReady(player1, new ConduitOfStorms());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(conduit.isTransformed()).isTrue();
        harness.passBothPriorities();
        assertThat(conduit.isTransformed()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Attacking adds {R} at the beginning of the next main phase this turn")
    void attackAddsRedManaAtNextMainPhase() {
        addCreatureReady(player1, new ConduitOfStorms());

        declareAttackers(player1, List.of(0));
        // Resolving the attack trigger + empty combat auto-advances into postcombat main,
        // which fires the delayed mana ability onto the stack.
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        harness.passBothPriorities(); // resolve AwardManaEffect
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("{3}{R}{R} transforms into Conduit of Emrakul")
    void transformAbilityFlipsToEmrakul() {
        Permanent conduit = addCreatureReady(player1, new ConduitOfStorms());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(conduit);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        assertThat(conduit.isTransformed()).isTrue();
        assertThat(conduit.getCard().getName()).isEqualTo("Conduit of Emrakul");
        assertThat(gqs.getEffectivePower(gd, conduit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, conduit)).isEqualTo(4);
    }

    @Test
    @DisplayName("Transformed Conduit of Emrakul adds {C}{C} when it attacks")
    void backFaceAttackAddsColorlessMana() {
        Permanent conduit = addCreatureReady(player1, new ConduitOfStorms());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(conduit);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Transforming after the attack trigger still adds {R}, not {C}{C}")
    void transformAfterAttackStillAddsRed() {
        Permanent conduit = addCreatureReady(player1, new ConduitOfStorms());

        declareAttackers(player1, List.of(0));
        // Attack trigger on stack; activate transform before it resolves.
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(conduit);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities(); // resolve transform (attack trigger still below)
        assertThat(conduit.isTransformed()).isTrue();

        harness.passBothPriorities(); // resolve attack trigger → schedules {R}, auto-advance to postcombat
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        harness.passBothPriorities(); // resolve delayed {R}

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
