package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
    }

    private void castFlash() {
        harness.setHand(player1, List.of(new FlashThompsonSpiderFan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
