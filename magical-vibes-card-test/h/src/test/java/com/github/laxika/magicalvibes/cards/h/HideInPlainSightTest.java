package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RubblebeltMaverick;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HideInPlainSight.class, Forest.class, GrizzlyBears.class, Island.class,
        LlanowarElves.class, Shock.class, RubblebeltMaverick.class, GrafdiggersCage.class})
class HideInPlainSightTest extends BaseCardTest {

    @Test
    void cloaksExactlyTwoOfTheTopFiveAndBottomsTheRestRandomly() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card shock = new Shock();
        Card island = new Island();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(bears, forest, shock, island, elves));
        harness.castFromHand(player1, new HideInPlainSight(), "{3}{G}");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.selectedToBattlefieldCloaked()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), forest.getId()));

        List<Permanent> cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .toList();
        assertThat(cloaked).hasSize(2);
        assertThat(cloaked).extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(bears, forest);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(shock, island, elves);
    }

    @Test
    void leavesCardsBelowTheTopFiveInOrderAboveTheBottomedCards() {
        Card creature = new RubblebeltMaverick();
        Card forest = new Forest();
        Card shock = new Shock();
        Card island = new Island();
        Card otherForest = new Forest();
        Card untouchedFirst = new Island();
        Card untouchedSecond = new Shock();
        harness.setLibrary(player1, List.of(creature, forest, shock, island, otherForest,
                untouchedFirst, untouchedSecond));
        beginResolution();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), forest.getId()));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(5);
        assertThat(library.subList(0, 2)).containsExactly(untouchedFirst, untouchedSecond);
        assertThat(library.subList(2, 5)).containsExactlyInAnyOrder(shock, island, otherForest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Hide in Plain Sight");
    }

    @Test
    void mustChooseTwoWhenAtLeastTwoCardsAreAvailable() {
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second, new Shock()));
        beginResolution();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cloaksTheOnlyCardInAShortLibrary() {
        Card card = new Shock();
        harness.setLibrary(player1, List.of(card));
        beginResolution();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cloaked.getCard()).isSameAs(card);
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        beginResolution();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hide in Plain Sight");
    }

    @Test
    void creatureTurnsFaceUpForItsManaCostWithoutTriggeringItsEnterAbility() {
        Card creature = new RubblebeltMaverick();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        beginResolution();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(2);
        assertThat(cloaked.isTapped()).isFalse();
        assertThat(cloaked.isSummoningSick()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, 0);

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isCloaked()).isFalse();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void creatureCannotTurnFaceUpWithoutPayingItsManaCost() {
        Card creature = new RubblebeltMaverick();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        beginResolution();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isCloaked()).isTrue();
    }

    @Test
    void noncreatureCardsCannotTurnFaceUpForMana() {
        Card land = new Forest();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(land, instant));
        beginResolution();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId(), instant.getId()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(Permanent::isCloaked);
    }

    @Test
    void wardCountersAnOpponentsSpellWhenPaymentIsDeclined() {
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        beginResolution();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, cloaked.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cloaked);
        assertThat(cloaked.isCloaked()).isTrue();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void doesNotRevealTheIdentitiesOfCloakedCardsInThePublicLog() {
        Card creature = new RubblebeltMaverick();
        Card land = new Island();
        harness.setLibrary(player1, List.of(creature, land));
        beginResolution();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));

        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("Rubblebelt Maverick")
                || entry.plainText().contains("Island"));
    }

    @Test
    void cagePreventsEvenNoncreatureCardsFromBeingCloaked() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        Card first = new Forest();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        beginResolution();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    private void beginResolution() {
        harness.castFromHand(player1, new HideInPlainSight(), "{3}{G}");
        harness.passBothPriorities();
    }
}
