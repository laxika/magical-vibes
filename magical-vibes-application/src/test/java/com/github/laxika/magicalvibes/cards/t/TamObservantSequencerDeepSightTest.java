package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamObservantSequencerDeepSight.class, Forest.class})
class TamObservantSequencerDeepSightTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall prepares Tam and exiles a castable Deep Sight copy")
    void landfallPreparesTam() {
        Permanent tam = harness.addToBattlefieldAndReturn(player1, new TamObservantSequencerDeepSight());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(tam.isPrepared()).isTrue();
        UUID copyId = tam.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId).card().getName()).isEqualTo("Deep Sight");
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Tam does not prepare when an opponent's land enters")
    void opponentLandDoesNotPrepareTam() {
        Permanent tam = harness.addToBattlefieldAndReturn(player1, new TamObservantSequencerDeepSight());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(tam.isPrepared()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Deep Sight draws a card, gains life, and unprepares Tam")
    void castingDeepSightDrawsGainsLifeAndUnpreparesTam() {
        Permanent tam = prepareTam();
        UUID copyId = tam.getPreparedSpellCardId();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(tam.isPrepared()).isFalse();
        assertThat(tam.getPreparedSpellCardId()).isNull();
        harness.assertLife(player1, 21);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("Casting Deep Sight unprepares Tam before the spell resolves")
    void castingUnpreparesImmediately() {
        Permanent tam = prepareTam();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, tam.getPreparedSpellCardId());

        assertThat(gd.stack).hasSize(1);
        assertThat(tam.isPrepared()).isFalse();
        assertThat(tam.getPreparedSpellCardId()).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deep Sight cannot be cast during combat and Tam remains prepared")
    void deepSightRequiresSorceryTiming() {
        Permanent tam = prepareTam();
        UUID copyId = tam.getPreparedSpellCardId();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tam.isPrepared()).isTrue();
        assertThat(tam.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another landfall while prepared does not create another spell copy")
    void repeatedLandfallDoesNotAccumulateCopies() {
        Permanent tam = prepareTam();
        UUID copyId = tam.getPreparedSpellCardId();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(tam.isPrepared()).isTrue();
        assertThat(tam.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.exilePlayPermissions).containsOnlyKeys(copyId);
    }

    @Test
    @DisplayName("Landfall can prepare Tam again after casting Deep Sight")
    void landfallPreparesAgainAfterCasting() {
        Permanent tam = prepareTam();
        UUID originalCopyId = tam.getPreparedSpellCardId();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, originalCopyId);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(tam.isPrepared()).isTrue();
        assertThat(tam.getPreparedSpellCardId()).isNotNull().isNotEqualTo(originalCopyId);
        assertThat(gd.findExiledCard(originalCopyId)).isNull();
        assertThat(gd.findExiledCard(tam.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Preparing Tam does not waive Deep Sight's mana cost")
    void insufficientManaDoesNotUnprepareTam() {
        Permanent tam = prepareTam();
        UUID copyId = tam.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tam.isPrepared()).isTrue();
        assertThat(tam.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent prepareTam() {
        Permanent tam = harness.addToBattlefieldAndReturn(player1, new TamObservantSequencerDeepSight());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return tam;
    }
}
