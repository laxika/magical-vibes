package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SiegeStriker.class, AlpineWatchdog.class, Plains.class})
class SiegeStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts to tap any number of untapped creatures")
    void attackTriggerPromptsForUntappedCreatures() {
        addCreatureReady(player1, new SiegeStriker());
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Siege Striker gets +1/+1 for each creature tapped this way")
    void boostsForEachCreatureTapped() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent first = addCreatureReady(player1, new AlpineWatchdog());
        Permanent second = addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(striker.getPowerModifier()).isEqualTo(2);
        assertThat(striker.getToughnessModifier()).isEqualTo(2);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing no creatures leaves Siege Striker unboosted")
    void choosingNoCreaturesDoesNothing() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(striker.getPowerModifier()).isZero();
        assertThat(striker.getToughnessModifier()).isZero();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(striker.getPowerModifier()).isEqualTo(1);
        assertThat(striker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(striker.getPowerModifier()).isZero();
        assertThat(striker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only untapped creatures controlled by the attacker can be chosen")
    void excludesTappedCreaturesOpponentsAndLands() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent eligible = addCreatureReady(player1, new AlpineWatchdog());
        Permanent tapped = addCreatureReady(player1, new AlpineWatchdog());
        tapped.tap();
        Permanent opponent = addCreatureReady(player2, new AlpineWatchdog());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(eligible.getId()));

        assertThat(striker.getPowerModifier()).isEqualTo(1);
        assertThat(opponent.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creatures may be tapped for the boost")
    void canTapSummoningSickCreature() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        creature.setSummoningSick(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(striker.getPowerModifier()).isEqualTo(1);
        assertThat(striker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a vigilance attacker does not remove it from combat")
    void vigilantAttackerStillDealsDamageAfterBeingTapped() {
        addCreatureReady(player1, new SiegeStriker());
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        resolveCombat();

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("An attack with no eligible creatures resolves without a choice")
    void noEligibleCreaturesStillDealsDoubleStrikeDamage() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(striker.getPowerModifier()).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The bonus persists when a tapped creature untaps or leaves")
    void bonusDoesNotDependOnTappedCreaturesRemaining() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent first = addCreatureReady(player1, new AlpineWatchdog());
        Permanent second = addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        first.untap();
        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(striker.getPowerModifier()).isEqualTo(2);
        assertThat(striker.getToughnessModifier()).isEqualTo(2);
    }
}
