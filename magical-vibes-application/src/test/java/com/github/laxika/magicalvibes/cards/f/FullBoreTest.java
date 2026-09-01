package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightLuminary;
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

@CardUsed({FullBore.class, GrizzlyBears.class, KnightLuminary.class})
class FullBoreTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature +3/+2 when it was not cast for warp")
    void givesBonusWithoutWarpKeywords() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFullBore(bears);

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Gives a warp-cast creature trample and haste in addition to +3/+2")
    void givesWarpKeywords() {
        castKnightLuminaryWithWarp();
        Permanent knight = findPermanent(player1, "Knight Luminary");

        castFullBore(knight);

        assertThat(knight.getPowerModifier()).isEqualTo(3);
        assertThat(knight.getToughnessModifier()).isEqualTo(2);
        assertThat(knight.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(knight.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant warp keywords to a normally cast warp creature")
    void normalCastDoesNotGetWarpKeywords() {
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight Luminary");
        castFullBore(knight);

        assertThat(knight.getPowerModifier()).isEqualTo(3);
        assertThat(knight.getToughnessModifier()).isEqualTo(2);
        assertThat(knight.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(knight.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Bonuses and keywords wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFullBore(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature controlled by the caster")
    void targetsOnlyYourCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FullBore()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void castKnightLuminaryWithWarp() {
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castFullBore(Permanent target) {
        harness.setHand(player1, List.of(new FullBore()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
