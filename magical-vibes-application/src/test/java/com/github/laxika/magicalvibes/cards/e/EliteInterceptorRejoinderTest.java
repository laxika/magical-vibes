package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EliteInterceptorRejoinder.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Elite Interceptor is prepared immediately upon entering without a triggered ability")
    void preparedBeforePlayersReceivePriority() {
        harness.castFromHand(player1, new EliteInterceptorRejoinder(), "{W}");
        harness.passBothPriorities();

        Permanent interceptor = findPermanent(player1, "Elite Interceptor");
        assertThat(interceptor.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(interceptor.getPreparedSpellCardId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejoinder untaps a tapped creature and draws a card")
    void untapsTappedCreatureAndDraws() {
        Permanent interceptor = castEliteInterceptor();
        Permanent target = addCreatureReady(player1, new EliteInterceptorRejoinder());
        target.tap();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, interceptor.getPreparedSpellCardId(), target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Rejoinder allows choosing to tap an already tapped creature")
    void mayTapAlreadyTappedCreature() {
        Permanent interceptor = castEliteInterceptor();
        Permanent target = addCreatureReady(player2, new EliteInterceptorRejoinder());
        target.tap();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, interceptor.getPreparedSpellCardId(), target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap");
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Rejoinder does not draw when its only target leaves the battlefield")
    void illegalTargetPreventsDraw() {
        Permanent interceptor = castEliteInterceptor();
        Permanent target = addCreatureReady(player2, new EliteInterceptorRejoinder());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, interceptor.getPreparedSpellCardId(), target.getId());
        assertThat(interceptor.isPrepared()).isFalse();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castEliteInterceptor() {
        harness.castFromHand(player1, new EliteInterceptorRejoinder(), "{W}");
        resolveAllTriggers();

        return findPermanent(player1, "Elite Interceptor");
    }
}
