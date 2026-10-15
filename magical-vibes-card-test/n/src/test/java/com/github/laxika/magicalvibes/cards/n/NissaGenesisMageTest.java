package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissaGenesisMage.class, Forest.class, GrizzlyBears.class, MindStone.class, Shock.class,
        TreetopVillage.class})
class NissaGenesisMageTest extends BaseCardTest {

    @Test
    @DisplayName("+2 untaps up to two creatures and up to two lands and gains loyalty")
    void plusTwoUntapsCreaturesAndLands() {
        Permanent nissa = addReadyNissa(player1, 5);
        Permanent bear1 = addTapped(player1, new GrizzlyBears());
        Permanent bear2 = addTapped(player1, new GrizzlyBears());
        Permanent forest1 = addTapped(player1, new Forest());
        Permanent forest2 = addTapped(player1, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(bear1.getId(), bear2.getId(), forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+2 may choose only lands")
    void plusTwoMayChooseOnlyLands() {
        addReadyNissa(player1, 5);
        Permanent forest = addTapped(player1, new Forest());
        Permanent bear = addTapped(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("+2 may choose no targets")
    void plusTwoMayChooseNoTargets() {
        Permanent nissa = addReadyNissa(player1, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("+2 rejects a third creature")
    void plusTwoRejectsThirdCreature() {
        addReadyNissa(player1, 5);
        Permanent b1 = addTapped(player1, new GrizzlyBears());
        Permanent b2 = addTapped(player1, new GrizzlyBears());
        Permanent b3 = addTapped(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(b1.getId(), b2.getId(), b3.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+2 rejects a non-creature non-land")
    void plusTwoRejectsArtifact() {
        addReadyNissa(player1, 5);
        Permanent stone = addTapped(player1, new MindStone());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(stone.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−3 gives target creature +5/+5 until end of turn")
    void minusThreePumpsCreature() {
        Permanent nissa = addReadyNissa(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(bear.getEffectivePower()).isEqualTo(7);
        assertThat(bear.getEffectiveToughness()).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("−10 puts chosen creatures and lands onto the battlefield; rest go to bottom randomly")
    void minusTenPutsCreaturesAndLands() {
        Permanent nissa = addReadyNissa(player1, 12);
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card forest2 = new Forest();
        harness.setLibrary(player1, List.of(forest, bears, shock, forest2));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                forest.getId(), bears.getId(), forest2.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), bears.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Shock");
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).extracting(Card::getName).containsExactlyInAnyOrder("Shock", "Forest");
    }

    @Test
    @DisplayName("−10 may put nothing; all looked-at cards go to bottom randomly")
    void minusTenMayPutNothing() {
        addReadyNissa(player1, 12);
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, shock));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Forest");
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Shock");
    }

    @Test
    void plusTwoRejectsThirdLand() {
        addReadyNissa(player1, 5);
        Permanent first = addTapped(player1, new Forest());
        Permanent second = addTapped(player1, new Forest());
        Permanent third = addTapped(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusTwoCanUntapOpponentsPermanents() {
        addReadyNissa(player1, 5);
        Permanent bear = addTapped(player2, new GrizzlyBears());
        Permanent forest = addTapped(player2, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId(), forest.getId()));
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void plusTwoCanAssignCreatureLandToLandQuota() {
        addReadyNissa(player1, 5);
        Permanent village = addTapped(player1, new TreetopVillage());
        Permanent firstBear = addTapped(player1, new GrizzlyBears());
        Permanent secondBear = addTapped(player1, new GrizzlyBears());
        Permanent forest = addTapped(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(village.getId(), firstBear.getId(), secondBear.getId(), forest.getId()));
        harness.passBothPriorities();

        assertThat(village.isTapped()).isFalse();
        assertThat(firstBear.isTapped()).isFalse();
        assertThat(secondBear.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void plusTwoRejectsChoosingSameCreatureTwiceAsCreature() {
        addReadyNissa(player1, 5);
        Permanent bear = addTapped(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusTwoRejectsChoosingSameLandTwiceAsLand() {
        addReadyNissa(player1, 5);
        Permanent forest = addTapped(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(forest.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusTwoCanChooseSameCreatureLandAsCreatureAndLand() {
        Permanent nissa = addReadyNissa(player1, 5);
        Permanent village = addTapped(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(village.getId(), village.getId()));
        harness.passBothPriorities();

        assertThat(village.isTapped()).isFalse();
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void minusThreeCanPumpOpponentsCreature() {
        addReadyNissa(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(7);
        assertThat(bear.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    void minusThreeRejectsLand() {
        addReadyNissa(player1, 5);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTenOnlyLooksAtTopTenAndPutsRestBelowUntouchedCards() {
        addReadyNissa(player1, 12);
        List<Card> topTen = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> (Card) new Forest()).toList();
        Card eleventh = new GrizzlyBears();
        Card twelfth = new Shock();
        List<Card> library = new java.util.ArrayList<>(topTen);
        library.add(eleventh);
        library.add(twelfth);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                topTen.stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, List.of(topTen.getFirst().getId()));

        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining.subList(0, 2)).containsExactly(eleventh, twelfth);
        assertThat(remaining.subList(2, remaining.size()))
                .containsExactlyInAnyOrderElementsOf(topTen.subList(1, 10));
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .contains(topTen.getFirst().getId());
    }

    @Test
    void minusTenCanPutAllTenEligibleCardsOntoBattlefield() {
        addReadyNissa(player1, 10);
        List<Card> cards = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> (Card) new Forest()).toList();
        harness.setLibrary(player1, cards);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, cards.stream().map(Card::getId).toList());

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .containsExactlyInAnyOrderElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Nissa, Genesis Mage");
    }

    @Test
    void minusTenWithNoEligibleCardsKeepsUntouchedLibraryOnTop() {
        addReadyNissa(player1, 12);
        List<Card> topTen = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> (Card) new Shock()).toList();
        Card eleventh = new Forest();
        List<Card> library = new java.util.ArrayList<>(topTen);
        library.add(eleventh);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining.getFirst()).isSameAs(eleventh);
        assertThat(remaining.subList(1, remaining.size())).containsExactlyInAnyOrderElementsOf(topTen);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void minusTenWithEmptyLibraryFinishesWithoutChoice() {
        Permanent nissa = addReadyNissa(player1, 12);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NissaGenesisMage());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.tap();
        return perm;
    }

}
