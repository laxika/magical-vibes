package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Forest.class, GrizzlyBears.class, Island.class, RevelRuiner.class})
class RevelRuinerTest extends BaseCardTest {

    @Test
    void entersAndConnivesPuttingCounterOnItAfterNonlandDiscard() {
        Card drawn = new Island();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(new RevelRuiner(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent revelRuiner = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId)).contains(drawn.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(discarded.getId());
        assertThat(revelRuiner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entersAndConnivesWithoutCounterAfterLandDiscard() {
        Card drawn = new Island();
        Card discarded = new Forest();
        harness.setHand(player1, List.of(new RevelRuiner(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent revelRuiner = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(discarded.getId());
        assertThat(revelRuiner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canDiscardTheCardJustDrawnWithAnInitiallyEmptyHand() {
        Card drawn = new RevelRuiner();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new RevelRuiner(), "{3}{B}");
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(findPermanent(player1, "Revel Ruiner")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void conniveStillDrawsAndDiscardsAfterSourceLeavesBattlefield() {
        Card drawn = new RevelRuiner();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new RevelRuiner(), "{3}{B}");
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Revel Ruiner");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn, source.getCard());
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void currentControllerConnivesWhenControlChangesBeforeTriggerResolves() {
        Card drawn = new RevelRuiner();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawn));
        harness.setLibrary(player1, List.of(new Island()));
        harness.castFromHand(player1, new RevelRuiner(), "{3}{B}");
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Revel Ruiner");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(drawn);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new RevelRuiner());
        addCreatureReady(player2, new RevelRuiner());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new RevelRuiner());
        Permanent first = addCreatureReady(player2, new RevelRuiner());
        Permanent second = addCreatureReady(player2, new RevelRuiner());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
