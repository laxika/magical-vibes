package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CaughtRedHanded.class, Cancel.class, Forest.class, GrizzlyBears.class})
class CaughtRedHandedTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, grants haste, and suspects the target creature")
    void resolvesAllEffects() {
        Permanent target = addTargetCreature();
        target.tap();

        castCaughtRedHanded(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(target.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup while suspect remains")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addTargetCreature();
        castCaughtRedHanded(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(target.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        Permanent target = addTargetCreature();
        CaughtRedHanded caughtRedHanded = new CaughtRedHanded();
        harness.setHand(player1, List.of(caughtRedHanded));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, caughtRedHanded.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = addTargetCreature();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CaughtRedHanded()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private Permanent addTargetCreature() {
        return harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
    }

    private void castCaughtRedHanded(Permanent target) {
        harness.setHand(player1, List.of(new CaughtRedHanded()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
