package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParagonOfFierceDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Other red creatures you control get +1/+1")
    void buffsOtherRedCreaturesYouControl() {
        Permanent paragon = addReady(player1, new ParagonOfFierceDefiance());
        Permanent giant = addReady(player1, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff nonred or opponent creatures")
    void onlyBuffsOwnRedCreatures() {
        addReady(player1, new ParagonOfFierceDefiance());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent opponentGiant = addReady(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentGiant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating grants haste to another red creature you control")
    void grantsHasteToAnotherRedCreature() {
        Permanent paragon = addReady(player1, new ParagonOfFierceDefiance());
        Permanent giant = addReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent paragon = addReady(player1, new ParagonOfFierceDefiance());
        Permanent giant = addReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, giant.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself, a nonred creature, or an opponent's creature")
    void restrictsActivationTarget() {
        Permanent paragon = addReady(player1, new ParagonOfFierceDefiance());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent opponentGiant = addReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, paragon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another red creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another red creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, opponentGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another red creature");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
