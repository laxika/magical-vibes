package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AjaniAdversaryOfTyrantsTest extends BaseCardTest {

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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyAjani(Player player, int loyalty) {
        Permanent perm = new Permanent(new AjaniAdversaryOfTyrants());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
