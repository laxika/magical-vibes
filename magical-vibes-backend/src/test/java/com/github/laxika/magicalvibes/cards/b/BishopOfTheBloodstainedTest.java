package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BishopOfTheBloodstainedTest extends BaseCardTest {

    @Test
    @DisplayName("Bishop of the Bloodstained has correct card properties")
    void hasCorrectProperties() {
        BishopOfTheBloodstained card = new BishopOfTheBloodstained();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
    }

    @Test
    @DisplayName("Has ETB effect that makes target opponent lose life per Vampire")
    void hasEtbLifeLossPerVampireEffect() {
        BishopOfTheBloodstained card = new BishopOfTheBloodstained();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(TargetPlayerLosesLifePerControlledPermanentEffect.class);
        TargetPlayerLosesLifePerControlledPermanentEffect effect =
                (TargetPlayerLosesLifePerControlledPermanentEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.multiplier()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB trigger targets opponent and goes on stack")
    void etbTriggerGoesOnStack() {
        castBishop();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bishop of the Bloodstained"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB with only Bishop on battlefield causes 1 life loss (counts itself)")
    void etbWithOnlyBishopCausesOneLifeLoss() {
        castBishop();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Bishop is itself a Vampire, so opponent loses 1 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB with additional Vampires causes more life loss")
    void etbWithAdditionalVampiresCausesMoreLifeLoss() {
        // Put two additional Vampires on the battlefield
        harness.addToBattlefield(player1, new AdantoVanguard());
        harness.addToBattlefield(player1, new AdantoVanguard());

        castBishop();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // 2 Adanto Vanguards + Bishop itself = 3 Vampires, opponent loses 3 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller does not gain life (not a drain effect)")
    void controllerDoesNotGainLife() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new AdantoVanguard());

        castBishop();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Controller's life should stay at 10 (no gain)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        // Opponent loses 2 life (Adanto Vanguard + Bishop)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new BishopOfTheBloodstained()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castBishop();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records life loss")
    void gameLogRecordsLifeLoss() {
        castBishop();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog).anyMatch(log -> log.contains("loses 1 life"));
    }

    private void castBishop() {
        harness.setHand(player1, List.of(new BishopOfTheBloodstained()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
