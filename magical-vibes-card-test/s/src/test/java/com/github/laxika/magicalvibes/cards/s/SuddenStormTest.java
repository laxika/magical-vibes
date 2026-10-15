package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenStorm.class, GrizzlyBears.class, Mountain.class, Unsummon.class})
class SuddenStormTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two creatures, skips their next untap, and scries 1")
    void tapsLocksAndScries() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Mountain();
        Card nextCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        castAndQueueScry(List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
    }

    @Test
    @DisplayName("Affected creature remains tapped through its next untap step")
    void skipsNextUntapStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(List.of(bears.getId()));

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Mountain());
        prepareSpell();
        UUID mountainId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void scriesWithNoTargets() {
        Card topCard = new Mountain();
        Card nextCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        castAndResolve(List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        harness.assertInGraveyard(player1, "Sudden Storm");
    }

    @Test
    void locksAlreadyTappedCreatureForOnlyItsControllersNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castAndResolve(List.of(creature.getId()));

        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void canTargetCreaturesControlledByDifferentPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(List.of(own.getId(), opposing.getId()));

        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isFalse();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isFalse();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());

        castAndQueueScry(List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Sudden Storm");
    }

    @Test
    void stillLocksAndScriesWhenOneTargetLeavesBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player2, 0, first.getId());

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Sudden Storm");
    }

    @Test
    void doesNotScryWhenAllChosenTargetsLeaveBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        prepareSpell();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Sudden Storm");
    }

    @Test
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAndResolve(List<UUID> targets) {
        castAndQueueScry(targets);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private void castAndQueueScry(List<UUID> targets) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, targets);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SuddenStorm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
