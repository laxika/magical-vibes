package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessInstincts.class, FeralKrushok.class})
class RuthlessInstinctsTest extends BaseCardTest {

    @Nested
    @CardUsed({RuthlessInstincts.class, FeralKrushok.class})
    @DisplayName("Mode 0: Target nonattacking creature gains reach and deathtouch and untaps")
    class NonattackingMode {

        @Test
        @DisplayName("Untaps the creature and grants reach and deathtouch")
        void untapsAndGrantsKeywords() {
            Permanent target = addCreature(player1);
            target.tap();

            cast(0, target);

            assertThat(target.isTapped()).isFalse();
            assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
            assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        }

        @Test
        @DisplayName("The granted keywords wear off at end of turn")
        void keywordsWearOffAtEndOfTurn() {
            Permanent target = addCreature(player1);

            cast(0, target);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
            assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        }

        @Test
        @DisplayName("Cannot target an attacking creature")
        void cannotTargetAttacker() {
            Permanent attacker = addAttackingCreature();

            assertThatThrownBy(() -> cast(0, attacker))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("nonattacking");
        }

        @Test
        void canUntapOpponentsNonattackingCreature() {
            Permanent target = addCreature(player2);
            target.tap();

            cast(0, target);

            assertThat(target.isTapped()).isFalse();
            assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
            assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        }

        @Test
        void canGrantKeywordsToUntappedBlockingCreature() {
            Permanent target = addCreature(player1);
            target.setBlocking(true);

            cast(0, target);

            assertThat(target.isTapped()).isFalse();
            assertThat(target.isBlocking()).isTrue();
            assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
            assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        }
    }

    @Nested
    @CardUsed({RuthlessInstincts.class, FeralKrushok.class})
    @DisplayName("Mode 1: Target attacking creature gets +2/+2 and trample")
    class AttackingMode {

        @Test
        @DisplayName("Boosts the attacker and grants trample")
        void boostsAndGrantsTrample() {
            Permanent attacker = addAttackingCreature();

            cast(1, attacker);

            assertThat(attacker.getPowerModifier()).isEqualTo(2);
            assertThat(attacker.getToughnessModifier()).isEqualTo(2);
            assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Cannot target a nonattacking creature")
        void cannotTargetNonattacker() {
            Permanent target = addCreature(player1);

            assertThatThrownBy(() -> cast(1, target))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("attacking creature");
        }

        @Test
        void boostAndTrampleWearOffAtEndOfTurn() {
            Permanent attacker = addAttackingCreature();
            cast(1, attacker);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(attacker.getPowerModifier()).isZero();
            assertThat(attacker.getToughnessModifier()).isZero();
            assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
        }

        @Test
        void canBoostOpponentsAttackerWithoutUntappingIt() {
            Permanent attacker = addCreature(player2);
            attacker.setAttacking(true);
            attacker.setAttackTarget(player1.getId());
            attacker.tap();

            cast(1, attacker);

            assertThat(attacker.getPowerModifier()).isEqualTo(2);
            assertThat(attacker.getToughnessModifier()).isEqualTo(2);
            assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
            assertThat(attacker.isTapped()).isTrue();
        }

        @Test
        void doesNotAffectTargetRemovedFromCombatBeforeResolution() {
            Permanent attacker = addAttackingCreature();
            castWithoutResolving(1, attacker);
            attacker.setAttacking(false);
            attacker.setAttackTarget(null);

            harness.passBothPriorities();

            assertThat(attacker.getPowerModifier()).isZero();
            assertThat(attacker.getToughnessModifier()).isZero();
            assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    private void cast(int mode, Permanent target) {
        castWithoutResolving(mode, target);
        harness.passBothPriorities();
    }

    private void castWithoutResolving(int mode, Permanent target) {
        harness.setHand(player1, List.of(new RuthlessInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, mode, target.getId());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new FeralKrushok());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addAttackingCreature() {
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }
}
