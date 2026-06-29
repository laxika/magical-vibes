package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PierceStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Pierce Strider has correct card properties")
    void hasCorrectProperties() {
        PierceStrider card = new PierceStrider();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
    }

    @Test
    @DisplayName("Has ETB effect that makes target opponent lose 3 life")
    void hasEtbLifeLossEffect() {
        PierceStrider card = new PierceStrider();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(TargetPlayerLosesLifeEffect.class);
        TargetPlayerLosesLifeEffect effect =
                (TargetPlayerLosesLifeEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.amount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected opponent target")
    void resolvingPutsEtbOnStackWithTarget() {
        castPierceStrider();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pierce Strider"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger makes target opponent lose 3 life")
    void etbMakesTargetOpponentLoseLife() {
        castPierceStrider();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB life loss works with non-default life totals")
    void etbLifeLossWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 5);

        castPierceStrider();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castPierceStrider();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records life loss")
    void gameLogRecordsLifeLoss() {
        castPierceStrider();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog).anyMatch(log -> log.contains("loses 3 life"));
    }

    @Test
    @DisplayName("Cannot cast by targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new PierceStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castPierceStrider() {
        harness.setHand(player1, List.of(new PierceStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
