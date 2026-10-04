package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireGiantsFury.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class FireGiantsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a Giant you control, grants trample, and grants a combat-damage trigger")
    void boostsGiantAndGrantsCombatDamageTrigger() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new FireGiantsFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(2);
        assertThat(giant.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isTrue();

        giant.setAttacking(true);
        giant.setAttackTarget(player2.getId());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(5);
        assertThat(gd.exilePlayPermissions).hasSize(5)
                .containsValue(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).hasSize(5)
                .containsValue(gd.turnNumber + 2);
    }

    @Test
    @DisplayName("The boost and granted trigger expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new FireGiantsFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, giant.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Giant or an opponent's Giant")
    void requiresGiantYouControl() {
        Permanent nonGiant = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGiant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new FireGiantsFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Giant you control");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Giant you control");
    }
}
