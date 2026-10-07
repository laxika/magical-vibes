package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AgathaOfTheVileCauldron;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheApprenticesFolly.class, GrizzlyBears.class, LlanowarElves.class,
        AgathaOfTheVileCauldron.class, ThreeBlindMice.class})
class TheApprenticesFollyTest extends BaseCardTest {

    @Test
    void copiesEligibleCreaturesAndSacrificesReflectionsOnChapterThree() {
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, legendaryBears);
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.castFromHand(player1, new TheApprenticesFolly(), "{2}{U}{R}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent firstReflection = findReflection(player1, "Grizzly Bears");
        assertThat(firstReflection.getCard().getSubtypes()).contains(CardSubtype.REFLECTION);
        assertThat(firstReflection.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(firstReflection.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);

        Permanent saga = findPermanent(player1, "The Apprentice's Folly");
        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(elves.getId()).doesNotContain(bears.getId());
        harness.handlePermanentChosen(player1, elves.getId());
        harness.passBothPriorities();

        assertThat(findReflections(player1)).hasSize(2);

        saga = findPermanent(player1, "The Apprentice's Folly");
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findReflections(player1)).isEmpty();
        harness.assertNotOnBattlefield(player1, "The Apprentice's Folly");
    }

    @Test
    void copiesRealLegendaryCreatureWithoutCountersAndKeepsItsOtherSubtypes() {
        Permanent agatha = harness.addToBattlefieldAndReturn(player1, new AgathaOfTheVileCauldron());
        agatha.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castFromHand(player1, new TheApprenticesFolly(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, agatha.getId());
        harness.passBothPriorities();

        Permanent reflection = findReflection(player1, "Agatha of the Vile Cauldron");
        assertThat(reflection.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(reflection.getCard().getSubtypes()).containsAll(agatha.getCard().getSubtypes())
                .contains(CardSubtype.REFLECTION);
        assertThat(reflection.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(reflection.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Agatha of the Vile Cauldron")).hasSize(2);
    }

    @Test
    void excludesTokensAndOpposingCreaturesButIgnoresOpposingTokensWithTheSameName() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingElves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        LlanowarElves tokenElves = new LlanowarElves();
        tokenElves.setToken(true);
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1, tokenElves);
        GrizzlyBears tokenBears = new GrizzlyBears();
        tokenBears.setToken(true);
        Permanent opposingToken = harness.addToBattlefieldAndReturn(player2, tokenBears);

        harness.castFromHand(player1, new TheApprenticesFolly(), "{2}{U}{R}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bears.getId())
                .doesNotContain(ownToken.getId(), opposingElves.getId(), opposingToken.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(findReflections(player1)).hasSize(1);
    }

    @Test
    void doesNotCopyWhenASameNamedTokenAppearsBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new TheApprenticesFolly(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        GrizzlyBears tokenBears = new GrizzlyBears();
        tokenBears.setToken(true);
        harness.addToBattlefield(player1, tokenBears);
        harness.passBothPriorities();

        assertThat(findReflections(player1)).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
    }

    @Test
    void chapterThreeSacrificesUnrelatedReflectionsOnlyUnderItsControllersControl() {
        GrizzlyBears ownReflectionCard = new GrizzlyBears();
        ownReflectionCard.setToken(true);
        ownReflectionCard.setSubtypes(List.of(CardSubtype.BEAR, CardSubtype.REFLECTION));
        Permanent ownReflection = harness.addToBattlefieldAndReturn(player1, ownReflectionCard);
        GrizzlyBears opposingReflectionCard = new GrizzlyBears();
        opposingReflectionCard.setToken(true);
        opposingReflectionCard.setSubtypes(List.of(CardSubtype.BEAR, CardSubtype.REFLECTION));
        Permanent opposingReflection = harness.addToBattlefieldAndReturn(player2, opposingReflectionCard);
        LlanowarElves ordinaryTokenCard = new LlanowarElves();
        ordinaryTokenCard.setToken(true);
        Permanent ordinaryToken = harness.addToBattlefieldAndReturn(player1, ordinaryTokenCard);
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheApprenticesFolly());
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownReflection).contains(ordinaryToken);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingReflection);
        harness.assertNotOnBattlefield(player1, "The Apprentice's Folly");
    }

    @Test
    void copyingAReflectionAlsoCopiesItsHaste() {
        Permanent agatha = harness.addToBattlefieldAndReturn(player1, new AgathaOfTheVileCauldron());
        harness.castFromHand(player1, new TheApprenticesFolly(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, agatha.getId());
        harness.passBothPriorities();
        Permanent originalReflection = findReflection(player1, "Agatha of the Vile Cauldron");
        Permanent mice = harness.addToBattlefieldAndReturn(player1, new ThreeBlindMice());
        mice.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, originalReflection.getId());
        harness.passBothPriorities();

        List<Permanent> reflections = findReflections(player1);
        assertThat(reflections).hasSize(2);
        Permanent copiedReflection = reflections.stream()
                .filter(permanent -> !permanent.getId().equals(originalReflection.getId()))
                .findFirst().orElseThrow();
        assertThat(harness.getGameQueryService().hasKeyword(gd, originalReflection, Keyword.HASTE)).isTrue();
        assertThat(harness.getGameQueryService().hasKeyword(gd, copiedReflection, Keyword.HASTE)).isTrue();
    }

    private Permanent findReflection(Player player, String name) {
        return findReflections(player).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private List<Permanent> findReflections(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.REFLECTION))
                .toList();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
