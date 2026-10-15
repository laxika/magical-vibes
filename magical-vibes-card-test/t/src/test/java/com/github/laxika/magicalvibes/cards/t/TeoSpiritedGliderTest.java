package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GliderKids;
import com.github.laxika.magicalvibes.cards.g.GiantKoi;
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

@CardUsed({TeoSpiritedGlider.class, GliderKids.class, GiantKoi.class, Mountain.class})
class TeoSpiritedGliderTest extends BaseCardTest {

    @Test
    @DisplayName("A flying attack draws and discards before choosing the counter target")
    void flyingAttackTriggersLootAndCounter() {
        addCreatureReady(player1, new TeoSpiritedGlider());
        Permanent flyer = addCreatureReady(player1, new GliderKids());
        Permanent target = addCreatureReady(player1, new GiantKoi());
        Permanent opponentCreature = addCreatureReady(player2, new GiantKoi());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GiantKoi()));

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(opponentCreature.getId());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flyer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("Discarding a land does not choose a target or put a counter on a creature")
    void landDiscardDoesNotAddCounter() {
        addCreatureReady(player1, new TeoSpiritedGlider());
        addCreatureReady(player1, new GliderKids());
        Permanent target = addCreatureReady(player1, new GiantKoi());
        harness.setHand(player1, List.of(new GiantKoi()));
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(1));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Giant Koi");
    }

    @Test
    @DisplayName("An attack without a flying creature does not trigger")
    void nonFlyingAttackDoesNotTrigger() {
        addCreatureReady(player1, new TeoSpiritedGlider());
        addCreatureReady(player1, new GiantKoi());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GiantKoi()));

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("Multiple flying attackers cause only one draw and discard")
    void multipleFlyingAttackersTriggerOnce() {
        addCreatureReady(player1, new TeoSpiritedGlider());
        addCreatureReady(player1, new GliderKids());
        harness.setHand(player1, List.of(new GiantKoi()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Giant Koi");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's flying attack does not trigger Teo")
    void opponentsFlyingAttackDoesNotTrigger() {
        addCreatureReady(player1, new TeoSpiritedGlider());
        addCreatureReady(player2, new GliderKids());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GiantKoi()));

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }
}
