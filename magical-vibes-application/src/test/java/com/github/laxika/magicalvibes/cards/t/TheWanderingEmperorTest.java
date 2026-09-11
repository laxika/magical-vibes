package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWanderingEmperor.class, GrizzlyBears.class})
class TheWanderingEmperorTest extends BaseCardTest {

    @Test
    void plusOnePutsCounterAndGrantsFirstStrikeToTargetCreature() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void plusOneMayChooseNoTarget() {
        Permanent emperor = addReadyEmperor(player1, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void minusOneCreatesVigilantSamuraiToken() {
        Permanent emperor = addReadyEmperor(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        Permanent samurai = findPermanent(player1, "Samurai");
        assertThat(samurai.getCard().getPower()).isEqualTo(2);
        assertThat(samurai.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, samurai, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void minusTwoExilesTappedCreatureAndGainsLife() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(creature.getCard().getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void loyaltyAbilitiesCanBeActivatedOnOpponentsTurnWhileEmperorEnteredThisTurn() {
        Permanent emperor = harness.enterBattlefieldAndReturn(player1, new TheWanderingEmperor());
        emperor.setCounterCount(CounterType.LOYALTY, 3);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(creature.getCard().getId());
    }

    @Test
    void loyaltyAbilitiesCannotBeActivatedOnOpponentsTurnAfterEntryTurn() {
        addReadyEmperor(player1, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    private Permanent addReadyEmperor(Player player, int loyalty) {
        Permanent permanent = new Permanent(new TheWanderingEmperor());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
