package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RideTheRails;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinecartDaredevil.class, RideTheRails.class, GrizzlyBears.class})
class MinecartDaredevilTest extends BaseCardTest {

    @Test
    void adventureBoostsTargetCreatureUntilEndOfTurnAndExilesTheCard() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MinecartDaredevil card = new MinecartDaredevil();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MinecartDaredevil card = new MinecartDaredevil();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Minecart Daredevil");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNull();
    }
}
