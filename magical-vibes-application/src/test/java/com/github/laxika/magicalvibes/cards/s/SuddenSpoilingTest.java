package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.c.CandlesOfLeng;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SuddenSpoiling.class, SerraAvenger.class, BenalishCavalry.class, CandlesOfLeng.class})
class SuddenSpoilingTest extends BaseCardTest {

    @Test
    @DisplayName("Makes all creatures controlled by the target player 0/2 without abilities")
    void spoilsTargetPlayersCreatures() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new SerraAvenger());
        Permanent targetNoncreature = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());

        castSuddenSpoiling(player2.getId());

        assertThat(targetCreature.getEffectivePower()).isZero();
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, targetCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, targetCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(targetNoncreature.getEffectiveToughness()).isZero();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLANKING)).isTrue();

        harness.setLibrary(player2, List.of(new BenalishCavalry()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        assertThat(targetNoncreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());

        castSuddenSpoiling(player2.getId());
        assertThat(creature.getEffectivePower()).isZero();
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-player object")
    void cannotTargetNonPlayer() {
        Permanent candles = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID candlesId = candles.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, candlesId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    @Test
    @DisplayName("Split second prevents spells and non-mana activated abilities")
    void splitSecondPreventsResponses() {
        Permanent candles = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new BenalishCavalry()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(candles.isTapped()).isFalse();
    }

    private void castSuddenSpoiling(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
