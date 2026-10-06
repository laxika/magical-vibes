package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZephyrScribe;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreenGoblinNemesis.class, ZephyrScribe.class, RagingGoblin.class,
        Shock.class, Forest.class, Mountain.class})
class GreenGoblinNemesisTest extends BaseCardTest {

    @Test
    void nonlandDiscardPutsCounterOnChosenGoblin() {
        harness.addToBattlefield(player1, new GreenGoblinNemesis());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        prepareDiscard(new Shock(), new Forest());

        discardOneCard(2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void discardTriggerCannotTargetOpponentsGoblin() {
        harness.addToBattlefield(player1, new GreenGoblinNemesis());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        prepareDiscard(new Shock(), new Forest());

        discardOneCard(1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landDiscardCreatesTappedTreasure() {
        harness.addToBattlefield(player1, new GreenGoblinNemesis());
        prepareDiscard(new Mountain(), new Forest());

        discardOneCard(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    void nonlandDiscardCanTargetGreenGoblinItself() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GreenGoblinNemesis());
        prepareDiscard(new Shock(), new Forest());

        discardOneCard(1);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void nonlandDiscardCannotTargetNonGoblin() {
        harness.addToBattlefield(player1, new GreenGoblinNemesis());
        prepareDiscard(new Shock(), new Forest());
        Permanent scribe = gd.playerBattlefields.get(player1.getId()).get(1);

        discardOneCard(1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, scribe.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removedTargetDoesNotRedirectCounterToAnotherGoblin() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenGoblinNemesis());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        prepareDiscard(new Shock(), new Forest());

        discardOneCard(2);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void landDiscardDoesNotPutCounterOnGoblin() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GreenGoblinNemesis());
        prepareDiscard(new Mountain(), new Forest());

        discardOneCard(1);
        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    void opponentsDiscardsDoNotTriggerEitherAbility() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GreenGoblinNemesis());
        prepareDiscard(new Shock(), new Forest());

        discardOneCard(0);
        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();

        Permanent scribe = gd.playerBattlefields.get(player1.getId()).getFirst();
        scribe.untap();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        discardOneCard(0);
        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private void prepareDiscard(com.github.laxika.magicalvibes.model.Card discardedCard,
            com.github.laxika.magicalvibes.model.Card libraryCard) {
        Permanent scribe = harness.addToBattlefieldAndReturn(player1, new ZephyrScribe());
        scribe.setSummoningSick(false);
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void discardOneCard(int scribeIndex) {
        harness.activateAbility(player1, scribeIndex, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }
}
