package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TrugaCliffcharger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GrizzlyBears.class, InvasionOfErgamon.class, Mountain.class,
        TrugaCliffcharger.class})
class InvasionOfErgamonTest extends BaseCardTest {

    @Test
    void entersWithTreasureAndMayDiscardToDraw() {
        Mountain discarded = new Mountain();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(new InvasionOfErgamon(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        addInvasionMana();

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void defeatingTheSiegeCastsTrugaCliffchargerTransformed() {
        Permanent battle = addBattleWithNoDefenseCounters();

        defeatBattle(battle);

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TrugaCliffcharger)
                .findFirst()
                .orElseThrow();
        assertThat(transformed.isTransformed()).isTrue();
    }

    @Test
    void transformedCreatureMayDiscardToSearchForALandOrBattle() {
        Mountain discarded = new Mountain();
        Forest land = new Forest();
        InvasionOfErgamon battleCard = new InvasionOfErgamon();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), land, battleCard));

        Permanent battle = addBattleWithNoDefenseCounters();
        defeatBattle(battle);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(land, battleCard);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    private void addInvasionMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent addBattleWithNoDefenseCounters() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfErgamon());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        return battle;
    }

    private void defeatBattle(Permanent battle) {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
