package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeceiverExarchTest extends BaseCardTest {

    @Test
    @DisplayName("Deceiver Exarch has a ChooseOneEffect with two ETB options")
    void hasCorrectEffects() {
        DeceiverExarch card = new DeceiverExarch();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(ChooseOneEffect.class);
        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.options()).hasSize(2);
        assertThat(effect.options().get(0).effect()).isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(effect.options().get(1).effect()).isInstanceOf(TapTargetPermanentEffect.class);
    }

    @Nested
    @DisplayName("Mode 1: Untap target permanent you control")
    class UntapMode {

        @Test
        @DisplayName("Untaps a tapped permanent you control")
        void untapsTappedPermanent() {
            harness.addToBattlefield(player1, new Island());
            Permanent island = gd.playerBattlefields.get(player1.getId()).getFirst();
            island.tap();
            UUID targetId = island.getId();

            castWithUntapMode(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(island.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Deceiver Exarch enters the battlefield when choosing untap mode")
        void exarchEntersBattlefield() {
            harness.addToBattlefield(player1, new Island());
            UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();

            castWithUntapMode(targetId);
            harness.passBothPriorities(); // resolve creature

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Deceiver Exarch"));
        }
    }

    @Nested
    @DisplayName("Mode 2: Tap target permanent an opponent controls")
    class TapMode {

        @Test
        @DisplayName("Taps an untapped permanent an opponent controls")
        void tapsOpponentPermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(bears.isTapped()).isFalse();
            UUID targetId = bears.getId();

            castWithTapMode(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Deceiver Exarch enters the battlefield when choosing tap mode")
        void exarchEntersBattlefield() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

            castWithTapMode(targetId);
            harness.passBothPriorities(); // resolve creature

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Deceiver Exarch"));
        }
    }

    private void castWithUntapMode(UUID targetId) {
        harness.setHand(player1, List.of(new DeceiverExarch()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId); // mode 0 = untap
    }

    private void castWithTapMode(UUID targetId) {
        harness.setHand(player1, List.of(new DeceiverExarch()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 1, targetId); // mode 1 = tap
    }
}
