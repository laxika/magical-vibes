package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(tam.isPrepared()).isFalse();
        assertThat(tam.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    private Permanent prepareTam() {
        Permanent tam = harness.addToBattlefieldAndReturn(player1, new TamObservantSequencerDeepSight());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        return tam;
    }
}
