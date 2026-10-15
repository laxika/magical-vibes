package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({LilianaDeathMage.class, GrizzlyBears.class, Forest.class})
class LilianaDeathMageTest extends BaseCardTest {

    @Test
    @DisplayName("+1 returns up to one target creature card from the graveyard to hand")
    void plusOneReturnsTargetCreatureToHand() {
        Permanent liliana = addReadyLiliana(player1, 4);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("+1 cannot target a noncreature card")
    void plusOneRejectsNoncreatureTarget() {
        addReadyLiliana(player1, 4);
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 destroys a creature and its controller loses 2 life")
    void minusThreeDestroysCreatureAndLosesLife() {
        Permanent liliana = addReadyLiliana(player1, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("-7 makes the targeted opponent lose 2 life per creature card in their graveyard")
    void minusSevenCountsOpponentCreatureCards() {
        Permanent liliana = addReadyLiliana(player1, 7);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore - 4);
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent permanent = new Permanent(new LilianaDeathMage());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
