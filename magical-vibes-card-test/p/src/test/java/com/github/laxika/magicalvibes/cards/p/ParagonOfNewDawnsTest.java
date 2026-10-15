package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParagonOfNewDawns.class, EliteVanguard.class, GrizzlyBears.class})
class ParagonOfNewDawnsTest extends BaseCardTest {

    @Test
    @DisplayName("Other white creatures you control get +1/+1")
    void buffsOtherWhiteCreaturesYouControl() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfNewDawns());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff nonwhite or opponent creatures")
    void onlyBuffsOwnWhiteCreatures() {
        addCreatureReady(player1, new ParagonOfNewDawns());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentVanguard = addCreatureReady(player2, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating grants vigilance to another white creature you control")
    void grantsVigilanceToAnotherWhiteCreature() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfNewDawns());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, vanguard.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfNewDawns());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());
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
        Permanent paragon = addCreatureReady(player1, new ParagonOfNewDawns());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentVanguard = addCreatureReady(player2, new EliteVanguard());
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

    @Test
    @DisplayName("Two Paragons boost each other, and the boost ends when one leaves")
    void paragonsBoostEachOtherWhileOnBattlefield() {
        Permanent first = addCreatureReady(player1, new ParagonOfNewDawns());
        Permanent second = addCreatureReady(player1, new ParagonOfNewDawns());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another Paragon is a legal target and vigilance resolves without its source")
    void vigilanceResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new ParagonOfNewDawns());
        Permanent target = addCreatureReady(player1, new ParagonOfNewDawns());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, source), 0, target.getId());
        assertThat(source.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A target moved to the opponent's battlefield is illegal at resolution")
    void doesNotGrantVigilanceAfterTargetChangesController() {
        Permanent source = addCreatureReady(player1, new ParagonOfNewDawns());
        Permanent target = addCreatureReady(player1, new ParagonOfNewDawns());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, source), 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
