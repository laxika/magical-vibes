package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        AangAtTheCrossroads.class,
        AangDestinedSavior.class,
        Forest.class,
        GrizzlyBears.class,
        Island.class,
        Mountain.class,
        SerraAngel.class
})
class AangAtTheCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put a creature with mana value four or less from the top five onto the battlefield")
    void etbPutsEligibleCreatureOntoBattlefield() {
        Card grizzlyBears = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card serraAngel = new SerraAngel();
        harness.setLibrary(player1, List.of(grizzlyBears, forest, island, mountain, serraAngel));

        harness.enterBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(grizzlyBears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(grizzlyBears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, island, mountain, serraAngel);
    }

    @Test
    @DisplayName("ETB can decline putting a creature onto the battlefield")
    void etbCanDeclineCreature() {
        Card grizzlyBears = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card serraAngel = new SerraAngel();
        harness.setLibrary(player1, List.of(grizzlyBears, forest, island, mountain, serraAngel));

        harness.enterBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(grizzlyBears, forest, island, mountain, serraAngel);
    }

    @Test
    @DisplayName("Another creature leaving schedules a transform for the next upkeep")
    void anotherCreatureLeavingTransformsAtNextUpkeep() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        leaveBattlefield(grizzlyBears);
        assertThat(aang.isTransformed()).isFalse();
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(aang.isTransformed()).isTrue();
        assertThat(aang.getCard()).isInstanceOf(AangDestinedSavior.class);
    }

    @Test
    @DisplayName("The transformed face earthbends a land and gives land creatures vigilance")
    void transformedFaceEarthbendsAtBeginningOfCombat() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        leaveBattlefield(grizzlyBears);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, land, Keyword.VIGILANCE)).isFalse();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(aang.getCard()).isInstanceOf(AangDestinedSavior.class);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.VIGILANCE)).isTrue();
    }

    private void leaveBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }
}
