package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuthlessInstinctsTest extends BaseCardTest {

    @Nested
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
    }

    @Nested
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
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new RuthlessInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addAttackingCreature() {
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }
}
