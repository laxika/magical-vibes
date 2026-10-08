package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnowDay.class, GrizzlyBears.class, Forest.class, Island.class})
class SnowDayTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two creatures, locks their next untap, then draws two and discards one")
    void tapsCreaturesAndDrawsThenDiscards() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card discarded = new GrizzlyBears();
        Card drawnForest = new Forest();
        Card drawnIsland = new Island();
        harness.setLibrary(player1, List.of(drawnForest, drawnIsland));
        harness.setHand(player1, List.of(new SnowDay(), discarded));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnForest, drawnIsland);
        harness.assertInGraveyard(player1, "Snow Day");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("May choose only one creature")
    void mayChooseOneCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new SnowDay(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SnowDay()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With no targets, draws two cards and allows discarding a newly drawn card")
    void mayChooseNoTargets() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player1, List.of(new SnowDay()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, island);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        harness.assertInGraveyard(player1, "Snow Day");
    }

    @Test
    @DisplayName("An already tapped friendly creature skips only its controller's next untap")
    void alreadyTappedFriendlyCreatureSkipsNextUntapOnly() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setTapped(true);
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new SnowDay()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));
        harness.handleCardChosen(player1, 0);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Still resolves and draws when one of two targets leaves the battlefield")
    void resolvesWithOneRemainingTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player1, List.of(new SnowDay()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, island);
        harness.handleCardChosen(player1, 0);
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not draw or discard when its only target becomes illegal")
    void doesNotResolveWithAllTargetsIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card forest = new Forest();
        Card island = new Island();
        Card retained = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player1, List.of(new SnowDay(), retained));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, island);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Snow Day");
    }

    @Test
    @DisplayName("Cannot choose more than two target creatures")
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SnowDay()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
