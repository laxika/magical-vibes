package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HowlingBansheeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Howling Banshee does not need a target")
    void doesNotNeedTarget() {
        HowlingBanshee card = new HowlingBanshee();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
    }

    @Test
    @DisplayName("Has ETB effects for each player losing life")
    void hasEtbEffects() {
        HowlingBanshee card = new HowlingBanshee();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD))
                .hasAtLeastOneElementOfType(EachOpponentLosesLifeEffect.class)
                .hasAtLeastOneElementOfType(LoseLifeEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Howling Banshee puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HowlingBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Howling Banshee");
    }

    // ===== ETB life loss =====

    @Test
    @DisplayName("Resolving puts Howling Banshee on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castHowlingBanshee();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Howling Banshee"));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Howling Banshee");
    }

    @Test
    @DisplayName("ETB causes each player to lose 3 life")
    void etbCausesEachPlayerToLose3Life() {
        castHowlingBanshee();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger (both effects in one ability)

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("ETB life loss works with non-default life totals")
    void etbLifeLossWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castHowlingBanshee();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castHowlingBanshee();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void castHowlingBanshee() {
        harness.setHand(player1, List.of(new HowlingBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    }
}
