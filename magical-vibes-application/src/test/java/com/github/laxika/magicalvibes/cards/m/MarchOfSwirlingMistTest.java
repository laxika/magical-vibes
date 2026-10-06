package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChainflailCentipede;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfSwirlingMist.class, MoonfolkPuzzlemaker.class, ChainflailCentipede.class})
class MarchOfSwirlingMistTest extends BaseCardTest {

    @Test
    void phasesOutUpToXTargetCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MoonfolkPuzzlemaker());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForX(player1, 0, 2, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(ownCreature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opposingCreature);
    }

    @Test
    void exilingBlueCardsFromHandReducesGenericCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist(), new MoonfolkPuzzlemaker()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(target.getId()), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .contains("Moonfolk Puzzlemaker");
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);
    }

    @Test
    void optionalHandExileCostOnlyAcceptsBlueCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist(), new ChainflailCentipede()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(target.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
    }
    @Test
    void canCastWithZeroTargetsAndZeroX() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "March of Swirling Mist");
    }

    @Test
    void multipleExiledCardsCanReduceCostBelowZeroWithoutReplacingBlueMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        MoonfolkPuzzlemaker first = new MoonfolkPuzzlemaker();
        MoonfolkPuzzlemaker second = new MoonfolkPuzzlemaker();
        harness.setHand(player1, List.of(first, new MarchOfSwirlingMist(), second));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantForXWithDiscards(player1, 1, 3, List.of(creature.getId()), List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    void exileReductionDoesNotPayRequiredBlueMana() {
        harness.setHand(player1, List.of(new MarchOfSwirlingMist(), new MoonfolkPuzzlemaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 1, List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cannotChooseMoreCreaturesThanX() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachCreaturePhasesInDuringItsControllersUntapStep() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new MoonfolkPuzzlemaker());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        opposing.tap();
        harness.setHand(player1, List.of(new MarchOfSwirlingMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForX(player1, 0, 2, List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
        assertThat(opposing.isTapped()).isFalse();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(own);
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card == own.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card == opposing.getCard());
    }

    @Test
    void remainingLegalTargetPhasesOutWhenAnotherTargetAlreadyPhasedOut() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker());
        harness.setHand(player1, List.of(new MarchOfSwirlingMist()));
        harness.setHand(player2, List.of(new MarchOfSwirlingMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passPriority(player1);
        harness.castInstantForX(player2, 0, 1, List.of(first.getId()));
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player2.getId())).containsExactly(first);
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertInGraveyard(player1, "March of Swirling Mist");
    }

    @Test
    void canTargetMoreThanOneHundredCreaturesWhenXAllowsIt() {
        List<Permanent> creatures = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new MoonfolkPuzzlemaker()))
                .toList();
        harness.setHand(player1, List.of(new MarchOfSwirlingMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 101);

        harness.castInstantForX(player1, 0, 101, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).containsAll(creatures);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
