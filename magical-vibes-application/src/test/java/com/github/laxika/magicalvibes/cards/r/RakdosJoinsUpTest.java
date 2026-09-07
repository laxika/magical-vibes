package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosJoinsUp.class, GrizzlyBears.class, IsamaruHoundOfKonda.class,
        DoomBlade.class, Shock.class})
class RakdosJoinsUpTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature from the graveyard with two +1/+1 counters")
    void returnsCreatureWithTwoCounters() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RakdosJoinsUp()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals dying legendary creature's power to a target opponent")
    void dealsDyingLegendaryCreaturePowerToOpponent() {
        harness.addToBattlefield(player1, new RakdosJoinsUp());
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        legendary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int opponentLife = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, legendary.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);
    }

    @Test
    @DisplayName("Does not trigger when a nonlegendary creature dies")
    void ignoresNonlegendaryCreatureDeath() {
        harness.addToBattlefield(player1, new RakdosJoinsUp());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int opponentLife = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }
}
