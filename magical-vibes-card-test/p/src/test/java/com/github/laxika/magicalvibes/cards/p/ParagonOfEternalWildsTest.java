package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({ParagonOfEternalWilds.class, GrizzlyBears.class, HillGiant.class})
class ParagonOfEternalWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Other green creatures you control get +1/+1")
    void buffsOtherGreenCreaturesYouControl() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff nongreen or opponent creatures")
    void onlyBuffsOwnGreenCreatures() {
        addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating grants trample to another green creature you control")
    void grantsTrampleToAnotherGreenCreature() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself, a nongreen creature, or an opponent's creature")
    void restrictsActivationTarget() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, paragon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another green creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another green creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another green creature");
    }

    @Test
    @DisplayName("Multiple Paragons buff each other and stack their boosts")
    void multipleParagonsBuffEachOther() {
        Permanent first = addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent second = addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(player1, first), 0, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves, but the static boost ends")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(paragon);
        gd.playerGraveyards.get(player1.getId()).add(paragon.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Target that changes controller before resolution does not gain trample")
    void targetMustRemainUnderYourControl() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfEternalWilds());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
