package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TurnTheTideTest extends BaseCardTest {

    @Test
    @DisplayName("Has BoostAllCreaturesEffect with -2/0 and opponent filter")
    void hasCorrectStructure() {
        TurnTheTide card = new TurnTheTide();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(BoostAllCreaturesEffect.class);

        BoostAllCreaturesEffect boost = (BoostAllCreaturesEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(-2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.filter()).isInstanceOf(PermanentNotPredicate.class);
    }

    @Test
    @DisplayName("Gives -2/-0 to opponent's creatures")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new TurnTheTide()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getEffectivePower()).isEqualTo(0);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect controller's own creatures")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new TurnTheTide()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new TurnTheTide()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getEffectivePower()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }
}
