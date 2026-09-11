package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScathingShadelockVenomousWordsTest extends BaseCardTest {

    @Test
    @DisplayName("Scathing Shadelock becomes prepared at the beginning of its controller's precombat main phase")
    void becomesPreparedAtPrecombatMain() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(shadelock.isPrepared()).isTrue();
        UUID copyId = shadelock.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting Venomous Words unprepares Scathing Shadelock and buffs a creature you control")
    void castingVenomousWordsBuffsControlledCreature() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        UUID copyId = shadelock.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(shadelock.isPrepared()).isFalse();
        assertThat(shadelock.getPreparedSpellCardId()).isNull();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("Venomous Words cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        UUID copyId = shadelock.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Venomous Words's boost and deathtouch wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, shadelock.getPreparedSpellCardId(), target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
