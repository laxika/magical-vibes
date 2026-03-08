package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TormentorExarchTest extends BaseCardTest {

    @Test
    @DisplayName("Tormentor Exarch has a ChooseOneEffect with two ETB options")
    void hasCorrectEffects() {
        TormentorExarch card = new TormentorExarch();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(ChooseOneEffect.class);
        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.options()).hasSize(2);
        assertThat(effect.options().get(0).effect()).isInstanceOf(BoostTargetCreatureEffect.class);
        assertThat(effect.options().get(1).effect()).isInstanceOf(BoostTargetCreatureEffect.class);

        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) effect.options().get(0).effect();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);

        BoostTargetCreatureEffect debuff = (BoostTargetCreatureEffect) effect.options().get(1).effect();
        assertThat(debuff.powerBoost()).isEqualTo(0);
        assertThat(debuff.toughnessBoost()).isEqualTo(-2);
    }

    @Nested
    @DisplayName("Mode 1: Target creature gets +2/+0 until end of turn")
    class BoostMode {

        @Test
        @DisplayName("Gives +2/+0 to target creature")
        void boostsTargetCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
            UUID targetId = bears.getId();

            castWithBoostMode(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.getPowerModifier()).isEqualTo(2);
            assertThat(bears.getToughnessModifier()).isEqualTo(0);
            assertThat(bears.getEffectivePower()).isEqualTo(4);
            assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Tormentor Exarch enters the battlefield when choosing boost mode")
        void exarchEntersBattlefield() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();

            castWithBoostMode(targetId);
            harness.passBothPriorities(); // resolve creature

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Tormentor Exarch"));
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature gets -0/-2 until end of turn")
    class DebuffMode {

        @Test
        @DisplayName("Gives -0/-2 to target creature")
        void debuffsTargetCreature() {
            harness.addToBattlefield(player2, new HillGiant());
            Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
            UUID targetId = giant.getId();

            castWithDebuffMode(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(giant.getPowerModifier()).isEqualTo(0);
            assertThat(giant.getToughnessModifier()).isEqualTo(-2);
            assertThat(giant.getEffectivePower()).isEqualTo(3);
            assertThat(giant.getEffectiveToughness()).isEqualTo(1);
        }

        @Test
        @DisplayName("Tormentor Exarch enters the battlefield when choosing debuff mode")
        void exarchEntersBattlefield() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

            castWithDebuffMode(targetId);
            harness.passBothPriorities(); // resolve creature

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Tormentor Exarch"));
        }

        @Test
        @DisplayName("-0/-2 kills a creature with 2 or less toughness")
        void debuffKillsLowToughnessCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

            castWithDebuffMode(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }
    }

    private void castWithBoostMode(UUID targetPermanentId) {
        harness.setHand(player1, List.of(new TormentorExarch()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0, targetPermanentId); // mode 0 = +2/+0
    }

    private void castWithDebuffMode(UUID targetPermanentId) {
        harness.setHand(player1, List.of(new TormentorExarch()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 1, targetPermanentId); // mode 1 = -0/-2
    }
}
