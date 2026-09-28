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

    private List<Card> libraryOf(int count) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            library.add(new GrizzlyBears());
        }
        return library;
    }
}
