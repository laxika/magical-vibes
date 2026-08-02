package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class ParagonOfNewDawnsTest extends BaseCardTest {

    @Test
    @DisplayName("Other white creatures you control get +1/+1")
    void buffsOtherWhiteCreaturesYouControl() {
        Permanent paragon = addReady(player1, new ParagonOfNewDawns());
        Permanent vanguard = addReady(player1, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff nonwhite or opponent creatures")
    void onlyBuffsOwnWhiteCreatures() {
        addReady(player1, new ParagonOfNewDawns());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent opponentVanguard = addReady(player2, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating grants vigilance to another white creature you control")
    void grantsVigilanceToAnotherWhiteCreature() {
        Permanent paragon = addReady(player1, new ParagonOfNewDawns());
        Permanent vanguard = addReady(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, vanguard.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        Permanent paragon = addReady(player1, new ParagonOfNewDawns());
        Permanent vanguard = addReady(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, vanguard.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself, a nonwhite creature, or an opponent's creature")
    void restrictsActivationTarget() {
        Permanent paragon = addReady(player1, new ParagonOfNewDawns());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent opponentVanguard = addReady(player2, new EliteVanguard());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, paragon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another white creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another white creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, opponentVanguard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another white creature");
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
