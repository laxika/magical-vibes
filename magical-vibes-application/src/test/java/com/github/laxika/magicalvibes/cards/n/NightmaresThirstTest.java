package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightmaresThirst.class, GreenwoodSentinel.class})
class NightmaresThirstTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life and gives -1/-1 when no prior life was gained this turn")
    void gainsLifeAndGivesMinusOneWithNoPriorGain() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new NightmaresThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Includes prior life gained this turn plus the 1 from the spell in X")
    void includesPriorLifeGainedPlusOwnGain() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        gd.lifeGainedThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new NightmaresThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // 5 prior + 1 from the spell = -6/-6; 2/2 dies
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Does not count opponent's life gained this turn")
    void ignoresOpponentLifeGained() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        gd.lifeGainedThisTurn.put(player2.getId(), 7);
        harness.setHand(player1, List.of(new NightmaresThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -X/-X wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new NightmaresThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new NightmaresThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts life gained in response but does not update an already resolved penalty")
    void evaluatesLifeGainOnResolutionAndFixesThePenalty() {
        Permanent firstTarget = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent responseTarget = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new NightmaresThirst(), new NightmaresThirst()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.castInstant(player1, 0, responseTarget.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gqs.getEffectivePower(gd, responseTarget)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, responseTarget)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(firstTarget.getId()))
                .anyMatch(p -> p.getId().equals(responseTarget.getId()));
        assertThat(gqs.getEffectivePower(gd, responseTarget)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, responseTarget)).isEqualTo(1);
    }

    @Test
    @CardUsed({Murder.class})
    @DisplayName("Does not gain life when the target is destroyed in response")
    void doesNotGainLifeWithAnIllegalTarget() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new NightmaresThirst()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Greenwood Sentinel");

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Nightmare's Thirst");
        assertThat(gd.stack).isEmpty();
    }
}
