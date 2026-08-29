package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BasrisLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature you control")
    void etbPutsCounterOnTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BasrisLieutenant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BasrisLieutenant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, opponentCreature.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Creates a vigilant Knight when an ally with a +1/+1 counter dies")
    void createsKnightWhenCounteredAllyDies() {
        harness.addToBattlefield(player1, new BasrisLieutenant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithMurder(player2, player1, bears.getId());
        harness.passBothPriorities();

        Permanent knight = findPermanents(player1, "Knight").getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(knight.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("The Lieutenant creates a Knight when it dies with a +1/+1 counter")
    void createsKnightWhenItselfDiesWithCounter() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new BasrisLieutenant());
        lieutenant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithMurder(player2, player1, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Knight when an ally dies without a +1/+1 counter")
    void doesNotCreateKnightWhenAllyHasNoPlusOnePlusOneCounter() {
        harness.addToBattlefield(player1, new BasrisLieutenant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithMurder(player2, player1, bears.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    private void destroyWithMurder(Player caster, Player targetController, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        gs.playCard(gd, caster, 0, 0, targetId, null);
        harness.passBothPriorities();
    }
}
