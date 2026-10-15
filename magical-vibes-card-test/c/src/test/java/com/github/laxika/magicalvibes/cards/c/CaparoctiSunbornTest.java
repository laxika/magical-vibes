package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.cards.i.IronpawAspirant;
import com.github.laxika.magicalvibes.cards.t.TinkersTote;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaparoctiSunborn.class, Forest.class, GrizzlyBears.class, IzzetCluestone.class,
        IronpawAspirant.class, TinkersTote.class})
class CaparoctiSunbornTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can tap two artifacts and/or creatures to discover 3")
    void attackingCanTapTwoPermanentsToDiscover() {
        Permanent caparocti = addCreatureReady(player1, new CaparoctiSunborn());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), discovered));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        assertThat(caparocti.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger does not tap permanents or discover")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new CaparoctiSunborn());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTapTwoSummoningSickCreaturesAndPutDiscoveredCardIntoHand() {
        addCreatureReady(player1, new CaparoctiSunborn());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IronpawAspirant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IronpawAspirant());
        Forest skippedLand = new Forest();
        CaparoctiSunborn skippedExpensiveCard = new CaparoctiSunborn();
        TinkersTote discovered = new TinkersTote();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedExpensiveCard, discovered, untouched));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skippedLand, skippedExpensiveCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTapTwoArtifactsAndCastDiscoveredCardWithoutMana() {
        addCreatureReady(player1, new CaparoctiSunborn());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TinkersTote());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TinkersTote());
        TinkersTote discovered = new TinkersTote();
        harness.setLibrary(player1, List.of(discovered));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == discovered);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotPayWithTappedPermanentsLandsOrOpponentsPermanents() {
        addCreatureReady(player1, new CaparoctiSunborn());
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new TinkersTote());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new IronpawAspirant());
        tapped.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentsArtifact = harness.addToBattlefieldAndReturn(player2, new TinkersTote());
        TinkersTote top = new TinkersTote();
        harness.setLibrary(player1, List.of(top));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(eligible.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(opponentsArtifact.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void playerChoosesWhichTwoPermanentsToTapWhenMoreAreAvailable() {
        addCreatureReady(player1, new CaparoctiSunborn());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TinkersTote());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IronpawAspirant());
        Permanent unused = harness.addToBattlefieldAndReturn(player1, new IronpawAspirant());
        TinkersTote discovered = new TinkersTote();
        harness.setLibrary(player1, List.of(discovered));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(unused.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void noQualifyingCardReturnsAllSkippedCardsToLibrary() {
        addCreatureReady(player1, new CaparoctiSunborn());
        harness.addToBattlefield(player1, new TinkersTote());
        harness.addToBattlefield(player1, new TinkersTote());
        Forest land = new Forest();
        CaparoctiSunborn expensive = new CaparoctiSunborn();
        harness.setLibrary(player1, List.of(land, expensive));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land, expensive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTapCaparoctiIfItIsUntappedWhenItsAttackTriggerResolves() {
        Permanent caparocti = addCreatureReady(player1, new CaparoctiSunborn());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TinkersTote());
        TinkersTote discovered = new TinkersTote();
        harness.setLibrary(player1, List.of(discovered));

        declareAttackers(List.of(0));
        caparocti.untap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(caparocti.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void payingWithAnEmptyLibraryStillTapsBothPermanents() {
        addCreatureReady(player1, new CaparoctiSunborn());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TinkersTote());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TinkersTote());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void discoverExilesSkippedCardsAndHitBeforeTheCastOrHandChoice() {
        addCreatureReady(player1, new CaparoctiSunborn());
        harness.addToBattlefield(player1, new TinkersTote());
        harness.addToBattlefield(player1, new TinkersTote());
        Forest skipped = new Forest();
        TinkersTote discovered = new TinkersTote();
        harness.setLibrary(player1, List.of(skipped, discovered));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card)
                .contains(skipped, discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card)
                .doesNotContain(skipped, discovered);
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(skipped);
    }
}
