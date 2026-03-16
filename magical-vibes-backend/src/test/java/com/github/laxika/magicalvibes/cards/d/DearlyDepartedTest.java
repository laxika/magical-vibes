package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GraveyardEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DearlyDepartedTest extends BaseCardTest {

    @Test
    @DisplayName("Has STATIC GraveyardEnterWithAdditionalCountersEffect for HUMAN subtype")
    void hasCorrectEffect() {
        DearlyDeparted card = new DearlyDeparted();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GraveyardEnterWithAdditionalCountersEffect.class);

        GraveyardEnterWithAdditionalCountersEffect effect =
                (GraveyardEnterWithAdditionalCountersEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(effect.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Human creature enters with +1/+1 counter when Dearly Departed is in graveyard")
    void humanGetsCounterWhenInGraveyard() {
        // Put Dearly Departed in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new DearlyDeparted());

        // Cast a Human creature
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getPlusOnePlusOneCounters()).isEqualTo(1);
        // Elite Vanguard is 2/1 + 1 counter = 3/2
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Human creature does not get a counter")
    void nonHumanDoesNotGetCounter() {
        gd.playerGraveyards.get(player1.getId()).add(new DearlyDeparted());

        // Cast a non-Human creature (Bear)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("No counter when Dearly Departed is on the battlefield instead of graveyard")
    void noCounterWhenOnBattlefield() {
        harness.addToBattlefield(player1, new DearlyDeparted());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // The Human should NOT have a counter (Dearly Departed is on battlefield, not in graveyard)
        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();
        assertThat(vanguard.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Multiple Dearly Departed in graveyard grant multiple counters")
    void multipleInGraveyardGrantMultipleCounters() {
        gd.playerGraveyards.get(player1.getId()).add(new DearlyDeparted());
        gd.playerGraveyards.get(player1.getId()).add(new DearlyDeparted());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getPlusOnePlusOneCounters()).isEqualTo(2);
        // Elite Vanguard is 2/1 + 2 counters = 4/3
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's Human does not get a counter from your Dearly Departed")
    void opponentHumanDoesNotBenefit() {
        // Player 1 has Dearly Departed in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new DearlyDeparted());

        // Player 2 casts a Human creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(vanguard.getPlusOnePlusOneCounters()).isZero();
    }
}
