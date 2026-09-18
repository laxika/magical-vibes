package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrestedCraghorn;
import com.github.laxika.magicalvibes.cards.f.FreneticRaptor;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LowlandTracker.class, FugitiveWizard.class, CrestedCraghorn.class, FreneticRaptor.class})
class LowlandTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers a defending player's creature as a provoke target")
    void attackingOffersDefendingCreatureAsTarget() {
        Permanent tracker = addCreatureReady(player1, new LowlandTracker());
        Permanent defendingCreature = addCreatureReady(player2, new FugitiveWizard());
        Permanent ownCreature = addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(tracker.getId(), ownCreature.getId());
    }

    @Test
    @DisplayName("Accepting provoke untaps the target and requires it to block")
    void acceptingUntapsAndRequiresBlock() {
        Permanent tracker = addCreatureReady(player1, new LowlandTracker());
        Permanent defendingCreature = addCreatureReady(player2, new FugitiveWizard());
        defendingCreature.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(defendingCreature.isTapped()).isFalse();
        assertThat(defendingCreature.getMustBlockIds()).containsExactly(tracker.getId());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(defendingCreature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Provoke does not require a creature that cannot block to block")
    void cannotBlockTargetSatisfiesIfAbleRequirement() {
        Permanent tracker = addCreatureReady(player1, new LowlandTracker());
        Permanent defendingCreature = addCreatureReady(player2, new FreneticRaptor());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        prepareDeclareBlockers();
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
        assertThat(defendingCreature.getMustBlockIds()).containsExactly(tracker.getId());
    }

    @Test
    @DisplayName("Declining provoke leaves the target untapped state and block requirements unchanged")
    void decliningDoesNothing() {
        Permanent tracker = addCreatureReady(player1, new LowlandTracker());
        Permanent defendingCreature = addCreatureReady(player2, new FugitiveWizard());
        defendingCreature.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingCreature.isTapped()).isTrue();
        assertThat(defendingCreature.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("First strike lets Lowland Tracker survive combat with a 4/1 blocker")
    void firstStrikeResolvesBeforeRegularCombatDamage() {
        Permanent tracker = addCreatureReady(player1, new LowlandTracker());
        Permanent blocker = addCreatureReady(player2, new CrestedCraghorn());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Lowland Tracker");
        harness.assertInGraveyard(player2, "Crested Craghorn");
    }

    @Test
    @DisplayName("Attacking does not prompt when the defending player controls no creatures")
    void noDefendingCreatureDoesNotPrompt() {
        addCreatureReady(player1, new LowlandTracker());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
