package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ParagonOfGatheringMists.class, FugitiveWizard.class, RuneclawBear.class})
class ParagonOfGatheringMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Other blue creatures you control get +1/+1")
    void buffsOtherBlueCreaturesYouControl() {
        Permanent paragon = addReady(player1, new ParagonOfGatheringMists());
        Permanent wizard = addReady(player1, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff nonblue or opponent creatures")
    void onlyBuffsOwnBlueCreatures() {
        addReady(player1, new ParagonOfGatheringMists());
        Permanent bears = addReady(player1, new RuneclawBear());
        Permanent opponentWizard = addReady(player2, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentWizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentWizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating grants flying to another blue creature you control")
    void grantsFlyingToAnotherBlueCreature() {
        Permanent paragon = addReady(player1, new ParagonOfGatheringMists());
        Permanent wizard = addReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, wizard.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent paragon = addReady(player1, new ParagonOfGatheringMists());
        Permanent wizard = addReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, wizard.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself, a nonblue creature, or an opponent's creature")
    void restrictsActivationTarget() {
        Permanent paragon = addReady(player1, new ParagonOfGatheringMists());
        Permanent bears = addReady(player1, new RuneclawBear());
        Permanent opponentWizard = addReady(player2, new FugitiveWizard());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, paragon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another blue creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another blue creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, opponentWizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another blue creature");
    }

    @Test
    @DisplayName("Multiple Paragons boost each other and their bonuses stack")
    void multipleParagonsBoostEachOther() {
        Permanent first = addReady(player1, new ParagonOfGatheringMists());
        Permanent second = addReady(player1, new ParagonOfGatheringMists());
        Permanent wizard = addReady(player1, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flying ability resolves independently of its source")
    void grantsFlyingAfterSourceLeavesBattlefield() {
        Permanent paragon = addReady(player1, new ParagonOfGatheringMists());
        Permanent wizard = addReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, wizard.getId());
        gd.playerBattlefields.get(player1.getId()).remove(paragon);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("A target controlled by an opponent at resolution does not gain flying")
    void rechecksTargetControllerOnResolution() {
        Permanent paragon = addReady(player1, new ParagonOfGatheringMists());
        Permanent wizard = addReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, wizard.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wizard);
        gd.playerBattlefields.get(player2.getId()).add(wizard);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isFalse();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires blue mana")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent paragon = addReady(player1, new ParagonOfGatheringMists());
        Permanent wizard = addReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(paragon.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The tap cost cannot be paid while summoning sick or tapped")
    void cannotActivateWhileSummoningSickOrTapped() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new ParagonOfGatheringMists());
        Permanent wizard = addReady(player1, new FugitiveWizard());
        paragon.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        paragon.setSummoningSick(false);
        paragon.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
