package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsOfTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TempestCallerTest extends BaseCardTest {

    // ===== Effect structure =====

    @Test
    @DisplayName("Has ETB TapPermanentsOfTargetPlayerEffect with creature filter")
    void hasEtbTapEffect() {
        TempestCaller card = new TempestCaller();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(TapPermanentsOfTargetPlayerEffect.class);
        TapPermanentsOfTargetPlayerEffect effect =
                (TapPermanentsOfTargetPlayerEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.filter()).isInstanceOf(PermanentIsCreaturePredicate.class);
    }

    @Test
    @DisplayName("Needs target and has opponent-only target filter")
    void needsTargetWithOpponentFilter() {
        TempestCaller card = new TempestCaller();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack targeting opponent")
    void resolvingPutsEtbOnStack() {
        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tempest Caller"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    // ===== ETB resolution =====

    @Test
    @DisplayName("ETB taps all creatures target opponent controls")
    void etbTapsAllOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> !p.isTapped());

        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(battlefield).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not tap non-creature permanents of target opponent")
    void doesNotTapNonCreatures() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        Permanent artifact = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(artifact.isTapped()).isFalse();

        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap controller's creatures")
    void doesNotTapControllerCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownCreature = gd.playerBattlefields.get(player1.getId()).getFirst();

        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tempest Caller remains on battlefield after ETB resolves")
    void remainsOnBattlefield() {
        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tempest Caller"));
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new TempestCaller()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Resolves without error when opponent has no creatures")
    void worksWithEmptyBattlefield() {
        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tempest Caller"));
    }

    // ===== Helpers =====

    private void castTempestCaller() {
        harness.setHand(player1, List.of(new TempestCaller()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
