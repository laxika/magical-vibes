package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrenzaloreClocktower.class, TheTenthDoctor.class, GrizzlyBears.class})
class TrenzaloreClocktowerTest extends BaseCardTest {

    @Test
    void addsBlueManaAndATimeCounter() {
        Permanent clocktower = harness.addToBattlefieldAndReturn(player1, new TrenzaloreClocktower());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(clocktower.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(clocktower.isTapped()).isTrue();
    }

    @Test
    void resetAbilityRequiresAControlledTimeLord() {
        Permanent clocktower = harness.addToBattlefieldAndReturn(player1, new TrenzaloreClocktower());
        clocktower.setCounterCount(CounterType.TIME, 12);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Time Lord");
    }

    @Test
    void resetAbilityExilesClocktowerShufflesHandAndGraveyardAndDrawsSeven() {
        Permanent clocktower = harness.addToBattlefieldAndReturn(player1, new TrenzaloreClocktower());
        clocktower.setCounterCount(CounterType.TIME, 12);
        harness.addToBattlefield(player1, new TheTenthDoctor());

        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, libraryOf(12));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(clocktower.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(clocktower.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
    }

    @Test
    void resetAbilityRequiresTwelveTimeCountersWithoutPayingOtherCostsOnFailure() {
        Permanent clocktower = harness.addToBattlefieldAndReturn(player1, new TrenzaloreClocktower());
        clocktower.setCounterCount(CounterType.TIME, 11);
        harness.addToBattlefield(player1, new TheTenthDoctor());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(clocktower.getCounterCount(CounterType.TIME)).isEqualTo(11);
        assertThat(clocktower.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clocktower);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(clocktower.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsTimeLordDoesNotPermitResetAbility() {
        Permanent clocktower = harness.addToBattlefieldAndReturn(player1, new TrenzaloreClocktower());
        clocktower.setCounterCount(CounterType.TIME, 12);
        harness.addToBattlefield(player2, new TheTenthDoctor());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Time Lord");

        assertThat(clocktower.getCounterCount(CounterType.TIME)).isEqualTo(12);
        assertThat(clocktower.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clocktower);
    }

    @Test
    void resetPaysCostsImmediatelyAndResolvesAfterTimeLordLeaves() {
        Permanent clocktower = harness.addToBattlefieldAndReturn(player1, new TrenzaloreClocktower());
        clocktower.setCounterCount(CounterType.TIME, 13);
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheTenthDoctor());
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        List<Card> library = libraryOf(5);
        List<Card> expectedCards = new ArrayList<>(library);
        expectedCards.add(handCard);
        expectedCards.add(graveyardCard);
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, library);
        Card opponentHandCard = new GrizzlyBears();
        Card opponentGraveyardCard = new GrizzlyBears();
        List<Card> opponentLibrary = libraryOf(8);
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.setLibrary(player2, opponentLibrary);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(clocktower.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(clocktower.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(clocktower.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(clocktower);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);

        gd.playerBattlefields.get(player1.getId()).remove(doctor);
        harness.setExile(player1, List.of(clocktower.getCard(), doctor.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(expectedCards);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
    }
    private List<Card> libraryOf(int count) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            library.add(new GrizzlyBears());
        }
        return library;
    }
}
