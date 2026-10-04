package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
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

@CardUsed({GruesomeRealization.class, GrizzlyBears.class, FugitiveWizard.class})
class GruesomeRealizationTest extends BaseCardTest {

    @Test
    @DisplayName("The draw mode draws two cards and makes the caster lose 2 life")
    void drawModeDrawsCardsAndLosesLife() {
        harness.setHand(player1, List.of(new GruesomeRealization()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The debuff mode affects only opponents' creatures")
    void debuffModeAffectsOnlyOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GruesomeRealization()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff mode can kill an opponent's 1/1")
    void debuffModeCanKillOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());

        harness.setHand(player1, List.of(new GruesomeRealization()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    @DisplayName("The debuff affects creatures present at resolution, but not creatures entering afterward")
    void debuffLocksInCreaturesAtResolution() {
        harness.setHand(player1, List.of(new GruesomeRealization()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(1);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(1);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff mode can resolve with no creatures and does not draw cards or lose life")
    void debuffModeResolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new GruesomeRealization()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Gruesome Realization");
    }

    @Test
    @DisplayName("The other player's draw mode affects only its caster and leaves creatures unchanged")
    void drawModeUsesSpellControllerAndDoesNotDebuff() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GruesomeRealization()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("An invalid mode is rejected")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new GruesomeRealization()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }
}
