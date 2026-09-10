package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EliteInterceptorRejoinderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Elite Interceptor and exiles a castable Rejoinder copy")
    void entersPrepared() {
        Permanent interceptor = castEliteInterceptor();

        assertThat(interceptor.isPrepared()).isTrue();
        UUID copyId = interceptor.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting Rejoinder unprepares Elite Interceptor, taps the target, and draws a card")
    void castingRejoinderUnpreparesTapsAndDraws() {
        Permanent interceptor = castEliteInterceptor();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        UUID copyId = interceptor.getPreparedSpellCardId();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(interceptor.isPrepared()).isFalse();
        assertThat(interceptor.getPreparedSpellCardId()).isNull();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("Declining Rejoinder's tap or untap action still draws a card")
    void decliningTapOrUntapStillDraws() {
        Permanent interceptor = castEliteInterceptor();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        UUID copyId = interceptor.getPreparedSpellCardId();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(interceptor.isPrepared()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent castEliteInterceptor() {
        harness.setHand(player1, List.of(new EliteInterceptorRejoinder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Elite Interceptor");
    }
}
