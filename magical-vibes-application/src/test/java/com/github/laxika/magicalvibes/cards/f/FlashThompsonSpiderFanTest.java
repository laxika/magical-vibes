package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlashThompsonSpiderFan.class, GrizzlyBears.class, Island.class})
class FlashThompsonSpiderFanTest extends BaseCardTest {

    private static final String HECKLE = "Heckle — Tap target creature.";
    private static final String HERO_WORSHIP = "Hero Worship — Untap target creature.";

    @Test
    void heckleTapsTheChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFlash();
        chooseMode(HECKLE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void heroWorshipUntapsTheChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        castFlash();
        chooseMode(HERO_WORSHIP);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void choosingBothModesResolvesTapThenUntapAndAllowsTheSameTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFlash();
        harness.handleListChoice(player1, HECKLE);
        harness.handleListChoice(player1, HERO_WORSHIP);
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void heckleCannotTargetAland() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        castFlash();
        chooseMode(HECKLE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesAffectOnlyTheirRespectiveTargets() {
        Permanent untapTarget = harness.addToBattlefieldAndReturn(player2, new FlashThompsonSpiderFan());
        untapTarget.tap();

        castFlash();
        Permanent tapTarget = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handleListChoice(player1, HECKLE);
        harness.handleListChoice(player1, HERO_WORSHIP);
        harness.handlePermanentChosen(player1, tapTarget.getId());
        harness.handlePermanentChosen(player1, untapTarget.getId());
        harness.passBothPriorities();

        assertThat(tapTarget.isTapped()).isTrue();
        assertThat(untapTarget.isTapped()).isFalse();
    }

    @Test
    void choosingUntapBeforeTapStillResolvesInPrintedOrder() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlashThompsonSpiderFan());

        castFlash();
        harness.handleListChoice(player1, HERO_WORSHIP);
        harness.handleListChoice(player1, HECKLE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void legalUntapTargetIsStillAffectedWhenTapTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlashThompsonSpiderFan());
        target.tap();

        castFlash();
        Permanent flash = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handleListChoice(player1, HECKLE);
        harness.handleListChoice(player1, HERO_WORSHIP);
        harness.handlePermanentChosen(player1, flash.getId());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(flash);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canBeCastDuringOpponentsEndStepAndTargetItself() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        castFlash();
        Permanent flash = gd.playerBattlefields.get(player1.getId()).getFirst();
        chooseMode(HECKLE);
        harness.handlePermanentChosen(player1, flash.getId());
        harness.passBothPriorities();

        assertThat(flash.isTapped()).isTrue();
    }

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
    }

    private void castFlash() {
        harness.castFromHand(player1, new FlashThompsonSpiderFan(), "{1}{W}");
        harness.passBothPriorities();
    }
}
