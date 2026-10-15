package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AjaniAdversaryOfTyrants.class, GrizzlyBears.class, HillGiant.class, Incinerate.class})
class AjaniAdversaryOfTyrantsTest extends BaseCardTest {

    @Test
    void plusOneAcceptsNoTargets() {
        Permanent ajani = addReadyAjani(player1, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneStillCountersRemainingTarget() {
        addReadyAjani(player1, 4);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void plusOneRejectsSameCreatureTwice() {
        addReadyAjani(player1, 4);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void minusTwoRejectsOpponentsGraveyard() {
        addReadyAjani(player1, 4);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void minusTwoRejectsNoncreatureCard() {
        addReadyAjani(player1, 4);
        Card instant = new Incinerate();
        harness.setGraveyard(player1, List.of(instant));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void minusTwoDoesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        addReadyAjani(player1, 4);
        Card target = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    void emblemContinuesOnSubsequentEndStepsWithoutAjani() {
        addReadyAjani(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ajani, Adversary of Tyrants");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        advanceIntoEndStep(player1);
        assertThat(findPermanents(player1, "Cat")).hasSize(3);
        advanceIntoEndStep(player2);
        assertThat(findPermanents(player1, "Cat")).hasSize(3);
        advanceIntoEndStep(player1);
        assertThat(findPermanents(player1, "Cat")).hasSize(6);
        assertThat(findPermanents(player2, "Cat")).isEmpty();
    }

    @Test
    @DisplayName("+1 puts a +1/+1 counter on each of two chosen creatures")
    void plusOneCountersTwoCreatures() {
        Permanent ajani = addReadyAjani(player1, 4);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 is up to two, so a single chosen creature still gets its counter")
    void plusOneAcceptsOneTarget() {
        addReadyAjani(player1, 4);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent untouched = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(untouched.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("−2 returns a creature card with mana value 2 from the graveyard to the battlefield")
    void minusTwoReanimatesCheapCreature() {
        Permanent ajani = addReadyAjani(player1, 4);
        Card bears = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 1, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("−2 cannot target a creature card with mana value 3 or more")
    void minusTwoRejectsExpensiveCreature() {
        addReadyAjani(player1, 4);
        Card giant = new HillGiant(); // mana value 4
        harness.setGraveyard(player1, List.of(giant));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, giant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("−7 creates an emblem that makes three lifelinking Cats at the controller's end step")
    void ultimateEmblemMakesCatsAtEndStep() {
        Permanent ajani = addReadyAjani(player1, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());

        advanceIntoEndStep(player1);

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(3);
        assertThat(cats).allSatisfy(cat -> {
            assertThat(gqs.hasKeyword(gd, cat, Keyword.LIFELINK)).isTrue();
            assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("The emblem does not trigger at the opposing player's end step")
    void emblemDoesNotTriggerOnOpponentsEndStep() {
        addReadyAjani(player1, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceIntoEndStep(player2);

        assertThat(findPermanents(player1, "Cat")).isEmpty();
    }

    /** Advances {@code activePlayer} into their end step so the step's triggers are collected. */
    private void advanceIntoEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private Permanent addReadyAjani(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AjaniAdversaryOfTyrants());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
