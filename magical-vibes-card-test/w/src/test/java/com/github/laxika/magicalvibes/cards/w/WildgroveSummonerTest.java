package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.VolatileRift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildgroveSummoner.class, Forest.class, DryadArbor.class, Island.class, VolatileRift.class})
class WildgroveSummonerTest extends BaseCardTest {

    @Test
    void perpetuallyTurnsForestsInHandAndLibraryIntoTreefolk() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new DryadArbor(), new Island()));

        harness.enterBattlefieldAndReturn(player1, new WildgroveSummoner());
        resolveAllTriggers();

        Permanent handForest = harness.enterBattlefieldAndReturn(
                player1, gd.playerHands.get(player1.getId()).removeFirst());
        Permanent libraryForest = harness.enterBattlefieldAndReturn(
                player1, gd.playerDecks.get(player1.getId()).removeFirst());

        for (Permanent forest : List.of(handForest, libraryForest)) {
            assertThat(gqs.getEffectiveCardTypes(gd, forest)).contains(CardType.LAND, CardType.CREATURE);
            assertThat(gqs.getEffectiveSubtypes(gd, forest)).contains(CardSubtype.FOREST, CardSubtype.TREEFOLK);
            assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isTrue();
            assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        }

        Card unchangedIsland = gd.playerDecks.get(player1.getId()).getLast();
        assertThat(unchangedIsland.getType()).isEqualTo(CardType.LAND);
        assertThat(unchangedIsland.getPower()).isNull();
    }

    @Test
    @org.junit.jupiter.api.DisplayName("A Clone of an animated Forest copies the printed Forest, not the perpetual Treefolk (CR 707.2)")
    @CardUsed(com.github.laxika.magicalvibes.cards.c.Clone.class)
    void cloneOfAnimatedForestIsOnlyAForest() {
        harness.setHand(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new WildgroveSummoner());
        resolveAllTriggers();
        Permanent forest = harness.enterBattlefieldAndReturn(
                player1, gd.playerHands.get(player1.getId()).removeFirst());
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.castFromHand(player1, new com.github.laxika.magicalvibes.cards.c.Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof com.github.laxika.magicalvibes.cards.c.Clone)
                .findFirst().orElseThrow();

        assertThat(gqs.isLand(gd, copy)).isTrue();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, copy, CardSubtype.TREEFOLK)).isFalse();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.HASTE)).isFalse();
    }

    @Test
    void deathTriggerSeeksUpToTwoForestsOntoTheBattlefield() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Island()));
        Permanent summoner = harness.addToBattlefieldAndReturn(player1, new WildgroveSummoner());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, summoner));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
    }

    @Test
    void deathTriggerPutsTheOnlyAvailableForestOntoTheBattlefield() {
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Island()));
        Permanent summoner = harness.addToBattlefieldAndReturn(player1, new WildgroveSummoner());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, summoner));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Island", "Island");
    }

    @Test
    void deathTriggerWithNoForestsLeavesLibraryUnchanged() {
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));
        Permanent summoner = harness.addToBattlefieldAndReturn(player1, new WildgroveSummoner());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, summoner));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    @Test
    void deathTriggerPutsPerpetuallyAnimatedForestsOntoTheBattlefield() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent summoner = harness.enterBattlefieldAndReturn(player1, new WildgroveSummoner());
        resolveAllTriggers();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, summoner));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(2);
        for (Permanent forest : findPermanents(player1, "Forest")) {
            assertThat(gqs.getEffectiveCardTypes(gd, forest)).contains(CardType.LAND, CardType.CREATURE);
            assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isTrue();
            assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
            assertThat(forest.isTapped()).isFalse();
        }
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Forests sought onto the battlefield pass through the hand, but have left it before a hand trigger's boost resolves (CR 400.7)")
    void seekingAnimatedForestsOntoTheBattlefieldLeavesTheHandBeforeAHandTriggerResolves() {
        harness.addToBattlefield(player1, new VolatileRift());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent summoner = harness.enterBattlefieldAndReturn(player1, new WildgroveSummoner());
        resolveAllTriggers();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, summoner));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(2);
        for (Permanent forest : findPermanents(player1, "Forest")) {
            assertThat(gd.perpetualCardPowerToughnessModifiers).doesNotContainKey(forest.getCard().getId());
            assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        }
    }

    @Test
    void animationDoesNotAffectOpponentsCardsOrOtherZones() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setExile(player1, List.of(new Forest()));
        Permanent battlefieldForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new WildgroveSummoner());
        resolveAllTriggers();

        Permanent opponentsHandForest = harness.enterBattlefieldAndReturn(
                player2, gd.playerHands.get(player2.getId()).removeFirst());
        Permanent opponentsLibraryForest = harness.enterBattlefieldAndReturn(
                player2, gd.playerDecks.get(player2.getId()).removeFirst());
        Permanent graveyardForest = harness.enterBattlefieldAndReturn(
                player1, gd.playerGraveyards.get(player1.getId()).removeFirst());
        Card exileCard = gd.getPlayerExiledCards(player1.getId()).getFirst();
        harness.setExile(player1, List.of());
        Permanent exiledForest = harness.enterBattlefieldAndReturn(player1, exileCard);

        for (Permanent forest : List.of(battlefieldForest, opponentsHandForest,
                opponentsLibraryForest, graveyardForest, exiledForest)) {
            assertThat(gqs.getEffectiveCardTypes(gd, forest)).doesNotContain(CardType.CREATURE);
            assertThat(gqs.hasKeyword(gd, forest, Keyword.REACH)).isFalse();
            assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
        }
    }

    @Test
    void animatedForestKeepsItsChangesAfterDyingAndReturning() {
        harness.setHand(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new WildgroveSummoner());
        resolveAllTriggers();
        Permanent forest = harness.enterBattlefieldAndReturn(
                player1, gd.playerHands.get(player1.getId()).removeFirst());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, forest));
        Permanent returnedForest = harness.enterBattlefieldAndReturn(
                player1, gd.playerGraveyards.get(player1.getId()).removeFirst());
        resolveAllTriggers();

        assertThat(gqs.getEffectiveCardTypes(gd, returnedForest)).contains(CardType.LAND, CardType.CREATURE);
        assertThat(gqs.getEffectivePower(gd, returnedForest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedForest)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, returnedForest, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, returnedForest, Keyword.HASTE)).isTrue();
    }
}
