package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChampionOfTheWeird.class, GoblinPiker.class, GrizzlyBears.class})
class ChampionOfTheWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Beholds a Goblin permanent and returns it to its owner's hand when Champion leaves")
    void beholdsPermanentAndReturnsItToHand() {
        Card beheldCard = new GoblinPiker();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.setHand(player1, List.of(new ChampionOfTheWeird()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Champion of the Weird");
        assertThat(harness.getGameData().findExiledCard(beheldCard.getId())).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(harness.getGameData(), champion));

        assertThat(harness.getGameData().findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).doesNotContain(beheldCard);
        harness.passBothPriorities();

        assertThat(harness.getGameData().findExiledCard(beheldCard.getId())).isNull();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("Can behold a Goblin card from hand")
    void beholdsCardFromHand() {
        Card beheldCard = new GoblinPiker();
        harness.setHand(player1, List.of(new ChampionOfTheWeird(), beheldCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(harness.getGameData().findExiledCard(beheldCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Pays 1 life and has the target opponent blight 2")
    void targetOpponentBlightsTwo() {
        Permanent beheldGoblin = addCreatureReady(player1, new GoblinPiker());
        harness.setHand(player1, List.of(new ChampionOfTheWeird()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreatureWithBeholdPermanent(player1, 0, beheldGoblin.getId());
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Champion of the Weird");
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        int lifeBefore = harness.getGameData().getLife(player1.getId());
        int countersBefore = targetCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        harness.activateAbility(player1,
                harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(champion),
                null,
                player2.getId());
        assertThat(champion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(targetCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(countersBefore);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(targetCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(countersBefore + 2);
    }

    @Test
    @DisplayName("Cannot target its controller with the blight ability")
    void cannotTargetItsController() {
        Permanent beheldGoblin = addCreatureReady(player1, new GoblinPiker());
        harness.setHand(player1, List.of(new ChampionOfTheWeird()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreatureWithBeholdPermanent(player1, 0, beheldGoblin.getId());
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Champion of the Weird");
        assertThatThrownBy(() -> harness.activateAbility(player1,
                harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(champion),
                null,
                player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new ChampionOfTheWeird());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot behold a non-Goblin from hand")
    void cannotBeholdNonGoblin() {
        harness.setHand(player1, List.of(new ChampionOfTheWeird(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreatureWithBeholdHandCard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
