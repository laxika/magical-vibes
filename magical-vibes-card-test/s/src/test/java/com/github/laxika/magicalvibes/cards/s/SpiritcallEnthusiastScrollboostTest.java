package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.PlayCardRequestDispatchService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritcallEnthusiastScrollboost.class, BladeSplicer.class, GrizzlyBears.class})
class SpiritcallEnthusiastScrollboostTest extends BaseCardTest {

    @Test
    @DisplayName("A token you control entering prepares Spiritcall Enthusiast")
    void tokenYouControlEnteringPreparesSpiritcall() {
        Permanent spiritcall = harness.addToBattlefieldAndReturn(player1, new SpiritcallEnthusiastScrollboost());

        createTokenUnderControl(player1);

        assertThat(spiritcall.isPrepared()).isTrue();
        UUID copyId = spiritcall.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("An opponent's token entering does not prepare Spiritcall Enthusiast")
    void opponentsTokenDoesNotPrepareSpiritcall() {
        Permanent spiritcall = harness.addToBattlefieldAndReturn(player1, new SpiritcallEnthusiastScrollboost());

        createTokenUnderControl(player2);

        assertThat(spiritcall.isPrepared()).isFalse();
        assertThat(spiritcall.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Casting Scrollboost unprepares Spiritcall Enthusiast and boosts its target")
    void castingScrollboostBoostsTarget() {
        Permanent spiritcall = harness.addToBattlefieldAndReturn(player1, new SpiritcallEnthusiastScrollboost());
        createTokenUnderControl(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID copyId = spiritcall.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(spiritcall.isPrepared()).isFalse();
        assertThat(spiritcall.getPreparedSpellCardId()).isNull();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void nontokenEnteringDoesNotPrepareSpiritcall() {
        Permanent spiritcall = harness.addToBattlefieldAndReturn(player1, new SpiritcallEnthusiastScrollboost());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(spiritcall.isPrepared()).isFalse();
        assertThat(spiritcall.getPreparedSpellCardId()).isNull();
    }

    @Test
    void anotherTokenWhilePreparedDoesNotCreateAnotherSpellCopy() {
        Permanent spiritcall = harness.addToBattlefieldAndReturn(player1, new SpiritcallEnthusiastScrollboost());
        createTokenUnderControl(player1);
        UUID copyId = spiritcall.getPreparedSpellCardId();
        int exileCount = gd.exiledCards.size();

        createTokenUnderControl(player1);

        assertThat(spiritcall.isPrepared()).isTrue();
        assertThat(spiritcall.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.exiledCards).hasSize(exileCount);
    }

    @Test
    void newTokenAfterCastingPreparesSpiritcallAgain() {
        Permanent spiritcall = harness.addToBattlefieldAndReturn(player1, new SpiritcallEnthusiastScrollboost());
        createTokenUnderControl(player1);
        UUID firstCopyId = spiritcall.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, firstCopyId, spiritcall.getId());
        harness.passBothPriorities();

        createTokenUnderControl(player1);

        assertThat(spiritcall.isPrepared()).isTrue();
        assertThat(spiritcall.getPreparedSpellCardId()).isNotNull().isNotEqualTo(firstCopyId);
        assertThat(gd.findExiledCard(firstCopyId)).isNull();
        assertThat(gd.findExiledCard(spiritcall.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    void scrollboostCannotBeCastWithoutATarget() {
        Permanent spiritcall = harness.addToBattlefieldAndReturn(player1, new SpiritcallEnthusiastScrollboost());
        createTokenUnderControl(player1);
        UUID copyId = spiritcall.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(spiritcall.isPrepared()).isTrue();
        assertThat(spiritcall.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    void scrollboostCanBoostTwoCreaturesThroughTheNormalCastRequest() {
        Permanent spiritcall = harness.addToBattlefieldAndReturn(player1, new SpiritcallEnthusiastScrollboost());
        createTokenUnderControl(player1);
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SpiritcallEnthusiastScrollboost());
        UUID copyId = spiritcall.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        PlayCardRequest request = new PlayCardRequest(
                0, null, spiritcall.getId(), null,
                List.of(spiritcall.getId(), other.getId()), null, null, null,
                null, copyId, null, null,
                null, null, null, null,
                null, null, null, null, null);

        new PlayCardRequestDispatchService(gs).dispatch(gd, player1, request);
        harness.passBothPriorities();

        assertThat(spiritcall.getEffectivePower()).isEqualTo(5);
        assertThat(spiritcall.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getEffectivePower()).isEqualTo(5);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
        assertThat(spiritcall.isPrepared()).isFalse();
    }

    private void createTokenUnderControl(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.castFromHand(player, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
