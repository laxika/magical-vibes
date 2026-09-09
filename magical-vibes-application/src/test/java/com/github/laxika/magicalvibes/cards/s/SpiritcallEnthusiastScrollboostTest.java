package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void createTokenUnderControl(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new BladeSplicer()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
