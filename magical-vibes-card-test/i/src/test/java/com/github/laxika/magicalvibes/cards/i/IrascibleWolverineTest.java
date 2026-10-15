package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IrascibleWolverine.class, Forest.class})
class IrascibleWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with end-of-turn play permission")
    void etbExilesTopCardWithPlayPermission() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new IrascibleWolverine(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("ETB exiles nothing when the library is empty")
    void etbExilesNothingWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new IrascibleWolverine(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void canPlayExiledLandButCannotPlayAnAdditionalLand() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new IrascibleWolverine(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void exiledCreatureRequiresManaAndTriggersItsOwnEnterAbility() {
        IrascibleWolverine topCard = new IrascibleWolverine();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.castFromHand(player1, new IrascibleWolverine(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof IrascibleWolverine).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(nextCard).doesNotContain(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void unusedPlayPermissionExpiresAndCardRemainsExiled() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new IrascibleWolverine(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void plottingPaysCostAndAllowsFreeCastOnlyOnALaterTurn() {
        IrascibleWolverine wolverine = new IrascibleWolverine();
        Forest topCard = new Forest();
        Forest drawCard = new Forest();
        harness.setLibrary(player1, List.of(drawCard, topCard));
        harness.setHand(player1, List.of(wolverine));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wolverine).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawCard, topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, wolverine.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player2, wolverine.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, wolverine.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, wolverine.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, wolverine.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Irascible Wolverine");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard).doesNotContain(wolverine);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void cannotPlotWithoutFullCostOrOutsideMainPhase() {
        IrascibleWolverine wolverine = new IrascibleWolverine();
        harness.setHand(player1, List.of(wolverine));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Irascible Wolverine");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Irascible Wolverine");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }
}
