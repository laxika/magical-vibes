package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FlyingMen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeoSpiritedGlider.class, FlyingMen.class, GrizzlyBears.class, Mountain.class})
class TeoSpiritedGliderTest extends BaseCardTest {

    @Test
    @DisplayName("A flying attack draws, discards, and puts a counter on a creature you control")
    void flyingAttackTriggersLootAndCounter() {
        addCreatureReady(player1, new TeoSpiritedGlider());
        Permanent flyer = addCreatureReady(player1, new FlyingMen());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 1);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flyer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("Discarding a land does not put a counter on the target")
    void landDiscardDoesNotAddCounter() {
        addCreatureReady(player1, new TeoSpiritedGlider());
        addCreatureReady(player1, new FlyingMen());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("An attack without a flying creature does not trigger")
    void nonFlyingAttackDoesNotTrigger() {
        addCreatureReady(player1, new TeoSpiritedGlider());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }
}
