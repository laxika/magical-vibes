package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShriekwoodDevourer.class, Forest.class, GrizzlyBears.class})
class ShriekwoodDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps up to the greatest power among creatures that attacked")
    void untapsLandsUpToGreatestAttackingPower() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstLand = addTappedLand(player1);
        Permanent secondLand = addTappedLand(player1);
        Permanent thirdLand = addTappedLand(player1);
        Permanent fourthLand = addTappedLand(player2);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstLand.getId(), secondLand.getId(), thirdLand.getId(), fourthLand.getId());
        assertThat(choice.validIds()).doesNotContain(attacker.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId(), fourthLand.getId()));

        assertThat(firstLand.isTapped()).isFalse();
        assertThat(fourthLand.isTapped()).isFalse();
        assertThat(secondLand.isTapped()).isTrue();
        assertThat(thirdLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May choose fewer lands than the greatest attacking power")
    void mayUntapOnlyOneLand() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addTappedLand(player1);
        Permanent second = addTappedLand(player1);
        Permanent third = addTappedLand(player1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May choose zero lands to untap")
    void mayDeclineToUntapLands() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addTappedLand(player1);
        Permanent second = addTappedLand(player1);
        Permanent third = addTappedLand(player1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still offers a choice when fewer lands exist than X")
    void doesNotAutomaticallyUntapAllAvailableLands() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        Permanent land = addTappedLand(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creatures put onto the battlefield attacking do not increase X")
    void ignoresCreaturesThatEnteredAttacking() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addTappedLand(player1);
        Permanent second = addTappedLand(player1);
        addTappedLand(player1);

        declareAttackers(List.of(1));
        Permanent laterAttacker = addCreatureReady(player1, new GrizzlyBears());
        laterAttacker.setPowerModifier(5);
        laterAttacker.setAttacking(true);
        laterAttacker.tap();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A declared attacker still contributes power after leaving combat")
    void countsDeclaredAttackerRemovedFromCombat() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addTappedLand(player1);
        Permanent second = addTappedLand(player1);
        addTappedLand(player1);

        declareAttackers(List.of(1));
        attacker.setAttacking(false);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Uses last known power when a declared attacker leaves the battlefield")
    void countsDeclaredAttackerThatDied() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addTappedLand(player1);
        Permanent second = addTappedLand(player1);
        addTappedLand(player1);

        declareAttackers(List.of(1));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Uses the greatest current power and triggers once for multiple attackers")
    void evaluatesGreatestPowerAtResolution() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addTappedLand(player1);
        Permanent second = addTappedLand(player1);
        Permanent third = addTappedLand(player1);
        Permanent fourth = addTappedLand(player1);

        declareAttackers(List.of(1, 2));
        firstAttacker.setPowerModifier(1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(fourth.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    private Permanent addTappedLand(com.github.laxika.magicalvibes.model.Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
        land.tap();
        return land;
    }
}
