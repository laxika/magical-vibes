package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SeethingSong;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiblioplexTomekeeper.class, BlazingFiresingerSeethingSong.class, SeethingSong.class})
class BiblioplexTomekeeperTest extends BaseCardTest {

    @Nested
    @CardUsed({BiblioplexTomekeeper.class, BlazingFiresingerSeethingSong.class, SeethingSong.class})
    @DisplayName("Mode: target creature becomes prepared")
    class PrepareMode {

        @Test
        @DisplayName("Prepares a creature with a prepare spell")
        void preparesCreatureWithPrepareSpell() {
            Permanent firesinger = addPreparedCapableCreature();
            UUID targetId = firesinger.getId();

            castTomekeeper(0, targetId);
            resolveAllTriggers();

            assertThat(firesinger.isPrepared()).isTrue();
            assertThat(firesinger.getPreparedSpellCardId()).isNotNull();
            assertThat(gd.exilePlayPermissions.get(firesinger.getPreparedSpellCardId()))
                    .isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Has no effect on a creature without a prepare spell")
        void noEffectOnCreatureWithoutPrepareSpell() {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new BiblioplexTomekeeper());

            castTomekeeper(0, creature.getId());
            resolveAllTriggers();

            assertThat(creature.isPrepared()).isFalse();
            assertThat(creature.getPreparedSpellCardId()).isNull();
        }

        @Test
        void preparingAlreadyPreparedCreatureKeepsExistingCopy() {
            Permanent firesinger = addPreparedCapableCreature();
            prepareCreature(firesinger);
            UUID copyId = firesinger.getPreparedSpellCardId();
            int exileCount = gd.exiledCards.size();

            castTomekeeper(0, firesinger.getId());
            resolveAllTriggers();

            assertThat(firesinger.isPrepared()).isTrue();
            assertThat(firesinger.getPreparedSpellCardId()).isEqualTo(copyId);
            assertThat(gd.exiledCards).hasSize(exileCount);
        }

        @Test
        void preparingOpponentsCreatureGrantsPermissionToItsController() {
            Permanent firesinger = harness.addToBattlefieldAndReturn(player2, new BlazingFiresingerSeethingSong());

            castTomekeeper(0, firesinger.getId());
            resolveAllTriggers();

            assertThat(firesinger.isPrepared()).isTrue();
            assertThat(gd.exilePlayPermissions.get(firesinger.getPreparedSpellCardId()))
                    .isEqualTo(player2.getId());
        }
    }

    @Nested
    @CardUsed({BiblioplexTomekeeper.class, BlazingFiresingerSeethingSong.class, SeethingSong.class})
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
            resolveAllTriggers();

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
            resolveAllTriggers();

            assertThat(firesinger.isPrepared()).isFalse();
            assertThat(firesinger.getPreparedSpellCardId()).isNull();
        }

        @Test
        void unpreparesOpponentsCreature() {
            Permanent firesinger = harness.addToBattlefieldAndReturn(player2, new BlazingFiresingerSeethingSong());
            prepareCreature(firesinger);
            UUID copyId = firesinger.getPreparedSpellCardId();

            castTomekeeper(1, firesinger.getId());
            resolveAllTriggers();

            assertThat(firesinger.isPrepared()).isFalse();
            assertThat(firesinger.getPreparedSpellCardId()).isNull();
            assertThat(gd.findExiledCard(copyId)).isNull();
            assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
        }
    }

    @Nested
    @CardUsed({BiblioplexTomekeeper.class})
    @DisplayName("Choose no mode")
    class SkipMode {

        @Test
        @DisplayName("Can enter without choosing a mode or target")
        void canSkipAllModes() {
            castTomekeeper(-1, null);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Biblioplex Tomekeeper");
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    void canChooseUnprepareModeWhenEnteringWithoutBeingCast() {
        Permanent firesinger = addPreparedCapableCreature();
        prepareCreature(firesinger);
        UUID copyId = firesinger.getPreparedSpellCardId();

        harness.enterBattlefieldAndReturn(player1, new BiblioplexTomekeeper());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, "Target creature becomes unprepared");
        harness.handlePermanentChosen(player1, firesinger.getId());
        resolveAllTriggers();

        assertThat(firesinger.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent addPreparedCapableCreature() {
        return harness.addToBattlefieldAndReturn(player1, new BlazingFiresingerSeethingSong());
    }

    private void prepareCreature(Permanent creature) {
        castTomekeeper(0, creature.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of());
    }

    private void castTomekeeper(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new BiblioplexTomekeeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, mode, targetId);
    }
}
