package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaceArcaneStrategist.class, GrizzlyBears.class})
class JaceArcaneStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("Second draw puts a +1/+1 counter on a creature you control")
    void secondDrawPutsCounterOnControlledCreature() {
        harness.addToBattlefield(player1, new JaceArcaneStrategist());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        draw(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        draw(player1);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 draws a card and adds loyalty")
    void plusOneDrawsCard() {
        Permanent jace = addReadyJace(player1, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-7 makes only your creatures unblockable this turn")
    void minusSevenMakesOwnCreaturesUnblockable() {
        addReadyJace(player1, 7);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent jace = new Permanent(new JaceArcaneStrategist());
        jace.setCounterCount(CounterType.LOYALTY, loyalty);
        jace.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jace);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jace;
    }
}
