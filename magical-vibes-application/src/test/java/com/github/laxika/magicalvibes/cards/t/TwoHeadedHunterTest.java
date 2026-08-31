package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwoHeadedHunter.class, TwiceTheRage.class, GrizzlyBears.class, Island.class})
class TwoHeadedHunterTest extends BaseCardTest {

    @Test
    void adventureGivesTargetCreatureDoubleStrikeUntilEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TwoHeadedHunter card = new TwoHeadedHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void adventureCannotTargetNonCreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        TwoHeadedHunter card = new TwoHeadedHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TwoHeadedHunter card = new TwoHeadedHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Two-Headed Hunter");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
