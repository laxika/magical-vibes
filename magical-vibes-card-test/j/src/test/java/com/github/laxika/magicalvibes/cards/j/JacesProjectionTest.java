package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JacesProjection.class, JaceBeleren.class, ChandraNalaar.class, GrizzlyBears.class})
class JacesProjectionTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on Jace's Projection")
    void drawingPutsCounterOnProjection() {
        Permanent projection = harness.addToBattlefieldAndReturn(player1, new JacesProjection());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        draw(player1);
        harness.passBothPriorities();

        assertThat(projection.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability puts a loyalty counter on a Jace planeswalker")
    void abilityPutsLoyaltyCounterOnJace() {
        Permanent projection = harness.addToBattlefieldAndReturn(player1, new JacesProjection());
        Permanent jace = addPlaneswalker(player1, new JaceBeleren(), 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(projection), 0, null, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Jace planeswalker")
    void abilityRejectsNonJacePlaneswalker() {
        Permanent projection = harness.addToBattlefieldAndReturn(player1, new JacesProjection());
        Permanent nonJacePlaneswalker = addPlaneswalker(player1, new ChandraNalaar(), 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(projection), 0, null, nonJacePlaneswalker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Jace planeswalker");
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private Permanent addPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        return planeswalker;
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
