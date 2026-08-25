package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeylineSurge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GiantGrowth.class, GrizzlyBears.class, InvasionOfShandalar.class, Island.class, LeylineSurge.class})
class InvasionOfShandalarTest extends BaseCardTest {

    @Test
    void returnsUpToThreePermanentCardsFromGraveyard() {
        Card forest = new Forest();
        Card island = new Island();
        Card bears = new GrizzlyBears();
        Card growth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(forest, island, bears, growth));
        harness.setHand(player1, List.of(new InvasionOfShandalar()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), island.getId(), bears.getId());

        harness.handleMultipleCardsChosen(player1, new ArrayList<>(List.of(forest.getId(), island.getId(), bears.getId())));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    void transformedLeylineSurgeMayPutPermanentFromHandOntoBattlefieldAtUpkeep() {
        harness.addToBattlefield(player1, new LeylineSurge());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GiantGrowth()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Giant Growth");
    }

    @Test
    void defeatingTheSiegeCastsLeylineSurgeTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfShandalar());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent leyline = findPermanent(player1, "Leyline Surge");
        assertThat(leyline.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(battle);
    }

    @Test
    void transformedLeylineSurgeDoesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new LeylineSurge());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
