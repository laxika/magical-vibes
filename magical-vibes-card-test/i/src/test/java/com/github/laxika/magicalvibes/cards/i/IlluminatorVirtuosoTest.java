package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BoonOfSafety;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.Reverberate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IlluminatorVirtuoso.class, GiantGrowth.class, GrizzlyBears.class, Mountain.class,
        BoonOfSafety.class, Reverberate.class})
class IlluminatorVirtuosoTest extends BaseCardTest {

    @Test
    void connivesWhenTargetedByControllerSpell() {
        Permanent virtuoso = addCreatureReady(player1, new IlluminatorVirtuoso());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, virtuoso.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(virtuoso.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotConniveWhenTargetedByOpponentSpell() {
        Permanent virtuoso = addCreatureReady(player1, new IlluminatorVirtuoso());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, virtuoso.getId());

        assertThat(virtuoso.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotAddCounterForLandDiscardedDuringConnive() {
        Permanent virtuoso = addCreatureReady(player1, new IlluminatorVirtuoso());
        harness.setHand(player1, List.of(new GiantGrowth(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, virtuoso.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        List<Card> hand = gd.playerHands.get(player1.getId());
        harness.handleCardChosen(player1, hand.size() - 1);

        assertThat(virtuoso.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotConniveWhenControllerSpellTargetsAnotherCreature() {
        Permanent virtuoso = addCreatureReady(player1, new IlluminatorVirtuoso());
        Permanent other = addCreatureReady(player1, new IlluminatorVirtuoso());
        harness.setHand(player1, List.of(new BoonOfSafety()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, other.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(virtuoso.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void connivesWhenControllersSpellCopyTargetsIt() {
        Permanent virtuoso = addCreatureReady(player1, new IlluminatorVirtuoso());
        BoonOfSafety boon = new BoonOfSafety();
        harness.setHand(player2, List.of(boon));
        harness.setHand(player1, List.of(new Reverberate()));
        harness.setLibrary(player1, List.of(new IlluminatorVirtuoso(), new Mountain()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player2, 0, virtuoso.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, boon.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(virtuoso.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(virtuoso.getCounterCount(CounterType.SHIELD)).isZero();
        harness.assertInGraveyard(player1, "Illuminator Virtuoso");
    }
}
