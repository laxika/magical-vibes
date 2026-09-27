package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmortusMasterOfEternity.class, GrizzlyBears.class})
class ImmortusMasterOfEternityTest extends BaseCardTest {

    @Test
    void tapsForBlueManaEqualToCardsDrawnThisTurnAndRestrictsItToNoncreatureSpells() {
        Permanent immortus = addCreatureReady(player1, new ImmortusMasterOfEternity());
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getNoncreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(immortus.isTapped()).isTrue();
    }

    @Test
    void powerUpShufflesEachPlayersHandAndGraveyardDrawsSevenAndAddsCounter() {
        Permanent immortus = harness.enterBattlefieldAndReturn(player1, new ImmortusMasterOfEternity());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, cards(8));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, cards(8));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(immortus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void powerUpCanBeActivatedOnlyOnce() {
        Permanent immortus = harness.enterBattlefieldAndReturn(player1, new ImmortusMasterOfEternity());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(immortus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private List<Card> cards(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new GrizzlyBears())
                .toList();
    }
}
