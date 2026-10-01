package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.g.GoblinFurrier;
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

@CardUsed({FuryOfTheHorde.class, GoblinFurrier.class, BorealDruid.class})
class FuryOfTheHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps creatures that attacked this turn and grants an additional combat and main phase")
    void untapsAttackedCreaturesAndGrantsAdditionalCombatAndMainPhase() {
        harness.addToBattlefield(player1, new GoblinFurrier());
        harness.addToBattlefield(player1, new GoblinFurrier());
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        battlefield.forEach(permanent -> permanent.setSummoningSick(false));

        declareAttackers(List.of(0));
        Permanent attackedFurrier = battlefield.get(0);
        Permanent nonAttackedFurrier = battlefield.get(1);
        nonAttackedFurrier.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FuryOfTheHorde()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(attackedFurrier.isTapped()).isFalse();
        assertThat(nonAttackedFurrier.isTapped()).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast by exiling two red cards from hand instead of paying mana")
    void castsByExilingTwoRedCardsFromHand() {
        harness.setHand(player1, List.of(new FuryOfTheHorde(), new GoblinFurrier(), new GoblinFurrier()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, null, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Goblin Furrier", "Goblin Furrier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires two red cards from hand")
    void alternateCostRequiresTwoRedCards() {
        harness.setHand(player1, List.of(new FuryOfTheHorde(), new GoblinFurrier(), new BorealDruid()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, null, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Additional combat begins after the resolving main phase")
    void additionalCombatBeginsAfterResolvingMainPhase() {
        harness.setHand(player1, List.of(new FuryOfTheHorde()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

}
