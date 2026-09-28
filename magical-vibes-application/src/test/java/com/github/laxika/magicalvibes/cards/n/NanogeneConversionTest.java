package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NanogeneConversion.class, GrizzlyBears.class, HillGiant.class, ThrunTheLastTroll.class})
class NanogeneConversionTest extends BaseCardTest {

    @Test
    @DisplayName("Makes every other creature a copy of the target")
    void makesOtherCreaturesCopies() {
        Permanent target = addReadyCreature(player1, new HillGiant());
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingCreature = addReadyCreature(player2, new GrizzlyBears());

        cast(target);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(3);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes legendary from the copied creatures")
    void removesLegendaryFromCopies() {
        Permanent target = addReadyCreature(player1, new ThrunTheLastTroll());
        Permanent copy = addReadyCreature(player1, new GrizzlyBears());

        cast(target);

        assertThat(target.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Copies revert at the end of the turn")
    void copiesRevertAtEndOfTurn() {
        Permanent target = addReadyCreature(player1, new HillGiant());
        Permanent copy = addReadyCreature(player1, new GrizzlyBears());

        cast(target);
        assertThat(copy.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(copy.getEffectivePower()).isEqualTo(2);
        assertThat(copy.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetAnOpposingCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NanogeneConversion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new NanogeneConversion()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
