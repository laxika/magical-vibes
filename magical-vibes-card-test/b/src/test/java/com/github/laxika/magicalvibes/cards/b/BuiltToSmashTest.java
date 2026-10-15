package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.t.ThrivingGrubs;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BuiltToSmash.class, DukharaPeafowl.class, ThrivingGrubs.class})
class BuiltToSmashTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an attacking artifact creature and grants it trample")
    void boostsAttackingArtifactCreatureAndGrantsTrample() {
        Permanent peafowl = addAttacker(new DukharaPeafowl());

        castResolve(peafowl);

        assertThat(peafowl.getPowerModifier()).isEqualTo(3);
        assertThat(peafowl.getToughnessModifier()).isEqualTo(3);
        assertThat(peafowl.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boosts an attacking nonartifact creature without granting trample")
    void boostsAttackingNonartifactCreatureWithoutTrample() {
        Permanent grubs = addAttacker(new ThrivingGrubs());

        castResolve(grubs);

        assertThat(grubs.getPowerModifier()).isEqualTo(3);
        assertThat(grubs.getToughnessModifier()).isEqualTo(3);
        assertThat(grubs.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent peafowl = addAttacker(new DukharaPeafowl());

        castResolve(peafowl);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(peafowl.getPowerModifier()).isZero();
        assertThat(peafowl.getToughnessModifier()).isZero();
        assertThat(peafowl.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent grubs = harness.addToBattlefieldAndReturn(player1, new ThrivingGrubs());
        harness.setHand(player1, List.of(new BuiltToSmash()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, grubs.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    @Test
    @DisplayName("Can boost an opponent's attacking artifact creature")
    void canTargetOpponentsAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.ensurePriority(player1);

        castResolve(attacker);

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.getToughnessModifier()).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not resolve if the target stops attacking in response")
    void targetMustStillBeAttackingAtResolution() {
        Permanent attacker = addAttacker(new DukharaPeafowl());
        harness.setHand(player1, List.of(new BuiltToSmash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Built to Smash");
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        return attacker;
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new BuiltToSmash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
