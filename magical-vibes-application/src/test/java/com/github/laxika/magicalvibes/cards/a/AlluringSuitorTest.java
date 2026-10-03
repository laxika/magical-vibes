package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlluringSuitor.class, GrizzlyBears.class, Abrade.class})
class AlluringSuitorTest extends BaseCardTest {

    @Test
    void transformsAndAddsPersistentRedManaWhenExactlyTwoCreaturesAttack() {
        Permanent suitor = addCreatureReady(player1, new AlluringSuitor());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(suitor.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getPersistentMana(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void doesNotTransformWhenMoreThanTwoCreaturesAttack() {
        Permanent suitor = addCreatureReady(player1, new AlluringSuitor());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));

        assertThat(suitor.isTransformed()).isFalse();
    }

    @Test
    void deadlyDancerBoostsItselfAndAnotherCreature() {
        Permanent dancer = addTransformedDancer(player1);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, other.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dancer)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
    }

    @Test
    void deadlyDancerCannotTargetItself() {
        Permanent dancer = addTransformedDancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dancer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTransformWhenOnlyOneCreatureAttacks() {
        Permanent suitor = addCreatureReady(player1, new AlluringSuitor());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(suitor.isTransformed()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void doesNotTransformWhenOpponentAttacksWithTwoCreatures() {
        Permanent suitor = addCreatureReady(player1, new AlluringSuitor());
        addCreatureReady(player2, new AlluringSuitor());
        addCreatureReady(player2, new AlluringSuitor());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(suitor.isTransformed()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void transformsWhenSuitorIsOneOfTheTwoAttackers() {
        Permanent suitor = addCreatureReady(player1, new AlluringSuitor());
        addCreatureReady(player1, new AlluringSuitor());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(suitor.isTransformed()).isTrue();
        assertThat(suitor.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    void attackTriggerStillTransformsSuitorAfterAnAttackerDies() {
        Permanent suitor = addCreatureReady(player1, new AlluringSuitor());
        Permanent attacker = addCreatureReady(player1, new AlluringSuitor());
        addCreatureReady(player1, new AlluringSuitor());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);

        declareAttackers(List.of(1, 2));
        harness.castInstant(player2, 0, 0, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        resolveAllTriggers();

        assertThat(suitor.isTransformed()).isTrue();
    }

    @Test
    void transformManaSurvivesUntilEndStepButExpiresAtEndOfTurn() {
        addCreatureReady(player1, new AlluringSuitor());
        addCreatureReady(player1, new AlluringSuitor());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void deadlyDancerCanBoostAnOpponentsCreatureAndBoostsExpire() {
        Permanent dancer = addTransformedDancer(player1);
        Permanent other = addCreatureReady(player2, new AlluringSuitor());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, other.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dancer)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, dancer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    void deadlyDancerDoesNotBoostItselfWhenItsOnlyTargetDies() {
        Permanent dancer = addTransformedDancer(player1);
        Permanent other = addCreatureReady(player2, new AlluringSuitor());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, other.getId());
        harness.castInstant(player2, 0, 0, other.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(other);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dancer)).isEqualTo(3);
    }

    @Test
    void deadlyDancerStillBoostsItsTargetAfterDancerDies() {
        Permanent dancer = addTransformedDancer(player1);
        Permanent other = addCreatureReady(player1, new AlluringSuitor());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, other.getId());
        harness.castInstant(player2, 0, 0, dancer.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dancer);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
    }

    private Permanent addTransformedDancer(Player player) {
        AlluringSuitor card = new AlluringSuitor();
        Permanent dancer = addCreatureReady(player, card);
        dancer.setCard(card.getBackFaceCard());
        dancer.setTransformed(true);
        return dancer;
    }
}
