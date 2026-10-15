package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.j.JeskaiSage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EliteScaleguard.class, JeskaiSage.class, BlackKnight.class})
class EliteScaleguardTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and bolsters the creature with the least toughness")
    void entersAndBolstersLeastToughnessCreature() {
        Permanent sage = addCreatureReady(player1, new JeskaiSage());

        harness.castFromHand(player1, new EliteScaleguard(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature with a +1/+1 counter attacking taps a defending creature")
    void counterBearingAttackerTapsDefendingCreature() {
        addCreatureReady(player1, new EliteScaleguard());
        Permanent attacker = addCreatureReady(player1, new JeskaiSage());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent victim = addCreatureReady(player2, new JeskaiSage());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(victim.getId());

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature without a +1/+1 counter does not trigger the tap ability")
    void creatureWithoutCounterDoesNotTrigger() {
        addCreatureReady(player1, new EliteScaleguard());
        Permanent victim = addCreatureReady(player2, new JeskaiSage());

        declareAttackers(List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void bolstersItselfWhenItIsTheOnlyCreature() {
        harness.castFromHand(player1, new EliteScaleguard(), "{4}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void choosesOneOfTheCreaturesTiedForLeastToughness() {
        Permanent first = addCreatureReady(player1, new JeskaiSage());
        Permanent second = addCreatureReady(player1, new JeskaiSage());
        Permanent opponent = addCreatureReady(player2, new JeskaiSage());

        harness.castFromHand(player1, new EliteScaleguard(), "{4}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bolsterUsesCurrentToughnessAndIgnoresOpposingCreatures() {
        Permanent larger = addCreatureReady(player1, new JeskaiSage());
        larger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent opponent = addCreatureReady(player2, new JeskaiSage());

        harness.castFromHand(player1, new EliteScaleguard(), "{4}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(larger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void scaleguardWithACounterTriggersForItsOwnAttack() {
        Permanent scaleguard = addCreatureReady(player1, new EliteScaleguard());
        scaleguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent victim = addCreatureReady(player2, new JeskaiSage());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void removingTheAttackersCounterAfterTriggeringDoesNotStopTheTap() {
        addCreatureReady(player1, new EliteScaleguard());
        Permanent attacker = addCreatureReady(player1, new JeskaiSage());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent victim = addCreatureReady(player2, new JeskaiSage());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, victim.getId());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void whiteScaleguardCannotTargetProtectionFromWhiteWhenABlueCreatureAttacks() {
        addCreatureReady(player1, new EliteScaleguard());
        Permanent attacker = addCreatureReady(player1, new JeskaiSage());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent protectedCreature = addCreatureReady(player2, new BlackKnight());
        Permanent legalTarget = addCreatureReady(player2, new JeskaiSage());

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(legalTarget.getId());
        harness.handlePermanentChosen(player1, legalTarget.getId());
        resolveAllTriggers();

        assertThat(legalTarget.isTapped()).isTrue();
        assertThat(protectedCreature.isTapped()).isFalse();
    }

    @Test
    void twoScaleguardsTriggerSeparatelyAndCanTapDifferentCreatures() {
        addCreatureReady(player1, new EliteScaleguard());
        addCreatureReady(player1, new EliteScaleguard());
        Permanent attacker = addCreatureReady(player1, new JeskaiSage());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent firstTarget = addCreatureReady(player2, new JeskaiSage());
        Permanent secondTarget = addCreatureReady(player2, new JeskaiSage());

        declareAttackers(List.of(2));
        harness.handlePermanentChosen(player1, firstTarget.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(secondTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        resolveAllTriggers();

        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
    }

    @Test
    void eachCounterBearingAttackerTriggersAndOnlyDefendingCreaturesAreTargets() {
        Permanent scaleguard = addCreatureReady(player1, new EliteScaleguard());
        scaleguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addCreatureReady(player1, new JeskaiSage());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent firstTarget = addCreatureReady(player2, new JeskaiSage());
        Permanent secondTarget = addCreatureReady(player2, new JeskaiSage());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactlyInAnyOrder(firstTarget.getId(), secondTarget.getId());
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        resolveAllTriggers();

        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
    }

    @Test
    void otherCounterTypesDoNotQualifyAnAttacker() {
        addCreatureReady(player1, new EliteScaleguard());
        Permanent attacker = addCreatureReady(player1, new JeskaiSage());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        Permanent victim = addCreatureReady(player2, new JeskaiSage());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void opposingCounterBearingAttackerDoesNotTriggerScaleguard() {
        addCreatureReady(player1, new EliteScaleguard());
        Permanent victim = addCreatureReady(player1, new JeskaiSage());
        Permanent attacker = addCreatureReady(player2, new JeskaiSage());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(victim.isTapped()).isFalse();
    }
}
