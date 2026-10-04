package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.r.ReluctantRoleModel;
import com.github.laxika.magicalvibes.cards.d.DefiantSurvivor;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EffieFastLearner.class, GrizzlyBears.class, ReluctantRoleModel.class,
        DefiantSurvivor.class, RelentlessAssault.class})
class EffieFastLearnerTest extends BaseCardTest {

    @Test
    void survivalCountersTappedCreaturesAndSeeksEligibleSurvivor() {
        Permanent effie = addCreatureReady(player1, new EffieFastLearner());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        effie.tap();
        tappedCreature.tap();

        Card eligibleSurvivor = new ReluctantRoleModel();
        Card expensiveSurvivor = new DefiantSurvivor();
        Card nonSurvivor = new GrizzlyBears();
        harness.setLibrary(player1, List.of(eligibleSurvivor, expensiveSurvivor, nonSurvivor));

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, effie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, effie)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tappedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, untappedCreature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleSurvivor);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveSurvivor, nonSurvivor);
    }

    @Test
    void untappedEffieDoesNotTriggerSurvival() {
        harness.addToBattlefield(player1, new EffieFastLearner());
        harness.setLibrary(player1, List.of(new ReluctantRoleModel()));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enlistTapsAProperSupporterAndAddsItsPower() {
        Permanent effie = addCreatureReady(player1, new EffieFastLearner());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, effie)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, effie)).isEqualTo(3);
    }

    @Test
    void enlistMayBeDeclined() {
        Permanent effie = addCreatureReady(player1, new EffieFastLearner());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, effie)).isEqualTo(3);
    }

    @Test
    void untappingEffieBeforeResolutionStopsBothEffects() {
        Permanent effie = addCreatureReady(player1, new EffieFastLearner());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        effie.tap();
        supporter.tap();
        Card survivor = new ReluctantRoleModel();
        harness.setLibrary(player1, List.of(survivor));

        advanceToPostcombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        effie.untap();
        harness.passBothPriorities();

        assertThat(effie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(supporter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(survivor);
    }

    @Test
    void seekUsesTappedCreatureCountAtResolutionAndIgnoresOpponents() {
        Permanent effie = addCreatureReady(player1, new EffieFastLearner());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        effie.tap();
        supporter.tap();
        opponent.tap();
        Card survivor = new ReluctantRoleModel();
        harness.setLibrary(player1, List.of(survivor));

        advanceToPostcombatMain(player1);
        supporter.untap();
        harness.passBothPriorities();

        assertThat(effie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(supporter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(survivor);
    }

    @Test
    void survivalDoesNotTriggerInThirdMainPhase() {
        Permanent effie = addCreatureReady(player1, new EffieFastLearner());
        effie.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        assertThat(effie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(effie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
    }
}
