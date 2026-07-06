package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.b.BlazingFiresingerSeethingSong;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreatureUnpreparedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BiblioplexTomekeeperTest extends BaseCardTest {

    

    @Nested
    @DisplayName("Mode: target creature becomes prepared")
    class PrepareMode {

        @Test
        @DisplayName("Prepares a creature with a prepare spell")
        void preparesCreatureWithPrepareSpell() {
            Permanent firesinger = addPreparedCapableCreature();
            UUID targetId = firesinger.getId();

            castTomekeeper(0, targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(firesinger.isPrepared()).isTrue();
            assertThat(firesinger.getPreparedSpellCardId()).isNotNull();
            assertThat(gd.exilePlayPermissions.get(firesinger.getPreparedSpellCardId()))
                    .isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Has no effect on a creature without a prepare spell")
        void noEffectOnCreatureWithoutPrepareSpell() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();

            castTomekeeper(0, bears.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(bears.isPrepared()).isFalse();
            assertThat(bears.getPreparedSpellCardId()).isNull();
        }
    }

    @Nested
    @DisplayName("Mode: target creature becomes unprepared")
    class UnprepareMode {

        @Test
        @DisplayName("Unprepares a prepared creature and removes its exiled copy")
        void unpreparesPreparedCreature() {
            Permanent firesinger = addPreparedCapableCreature();
            prepareCreature(firesinger);
            UUID copyId = firesinger.getPreparedSpellCardId();
            assertThat(copyId).isNotNull();

            castTomekeeper(1, firesinger.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(firesinger.isPrepared()).isFalse();
            assertThat(firesinger.getPreparedSpellCardId()).isNull();
            assertThat(gd.findExiledCard(copyId)).isNull();
            assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
        }

        @Test
        @DisplayName("Has no effect on an unprepared creature")
        void noEffectOnUnpreparedCreature() {
            Permanent firesinger = addPreparedCapableCreature();

            castTomekeeper(1, firesinger.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(firesinger.isPrepared()).isFalse();
            assertThat(firesinger.getPreparedSpellCardId()).isNull();
        }
    }

    @Nested
    @DisplayName("Choose no mode")
    class SkipMode {

        @Test
        @DisplayName("Can enter without choosing a mode or target")
        void canSkipAllModes() {
            castTomekeeper(-1, null);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Biblioplex Tomekeeper"));
            assertThat(gd.stack).isEmpty();
        }
    }

    private Permanent addPreparedCapableCreature() {
        harness.addToBattlefield(player1, new BlazingFiresingerSeethingSong());
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blazing Firesinger"))
                .findFirst()
                .orElseThrow();
    }

    private void prepareCreature(Permanent creature) {
        harness.setHand(player1, List.of(new BiblioplexTomekeeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
    }

    private void castTomekeeper(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new BiblioplexTomekeeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, mode, targetId);
    }
}
