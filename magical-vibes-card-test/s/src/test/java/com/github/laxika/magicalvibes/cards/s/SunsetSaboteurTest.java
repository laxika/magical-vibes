package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunsetSaboteur.class, GrizzlyBears.class, Shock.class})
class SunsetSaboteurTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets a creature an opponent controls and puts a +1/+1 counter on it")
    void attackTriggerTargetsOpponentCreature() {
        Permanent saboteur = addCreatureReady(player1, new SunsetSaboteur());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreature.getId())
                .doesNotContain(saboteur.getId(), ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ward triggers when an opponent targets Sunset Saboteur")
    void wardTriggersWhenOpponentTargetsIt() {
        Permanent saboteur = addCreatureReady(player1, new SunsetSaboteur());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, saboteur.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Sunset Saboteur");
    }

    @Test
    void wardCountersSpellWhenOpponentHasNoCardToDiscard() {
        Permanent saboteur = addCreatureReady(player1, new SunsetSaboteur());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, saboteur.getId());

        harness.assertOnBattlefield(player1, "Sunset Saboteur");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(saboteur.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentMayDeclineWardEvenWithCardInHand() {
        Permanent saboteur = addCreatureReady(player1, new SunsetSaboteur());
        GrizzlyBears discard = new GrizzlyBears();
        harness.setHand(player2, List.of(new Shock(), discard));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, saboteur.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sunset Saboteur");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(discard);
        assertThat(saboteur.getMarkedDamage()).isZero();
    }

    @Test
    void discardingForWardAllowsSpellToResolve() {
        Permanent saboteur = addCreatureReady(player1, new SunsetSaboteur());
        GrizzlyBears discard = new GrizzlyBears();
        harness.setHand(player2, List.of(new Shock(), discard));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, saboteur.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sunset Saboteur");
        harness.assertInGraveyard(player1, "Sunset Saboteur");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void wardDoesNotTriggerForControllersOwnSpell() {
        Permanent saboteur = addCreatureReady(player1, new SunsetSaboteur());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, saboteur.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sunset Saboteur");
    }

    @Test
    void attackWithoutOpponentCreatureDoesNotRequireTarget() {
        Permanent saboteur = addCreatureReady(player1, new SunsetSaboteur());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(saboteur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentWardCanCounterAttackTrigger() {
        addCreatureReady(player1, new SunsetSaboteur());
        Permanent opponent = addCreatureReady(player2, new SunsetSaboteur());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, opponent.getId());
        resolveAllTriggers();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackTriggerDoesNotRetargetWhenChosenCreatureDies() {
        addCreatureReady(player1, new SunsetSaboteur());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target).contains(other);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void menaceRejectsSingleBlocker() {
        addCreatureReady(player1, new SunsetSaboteur());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        resolveAllTriggers();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new SunsetSaboteur());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, first.getId());
        resolveAllTriggers();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
