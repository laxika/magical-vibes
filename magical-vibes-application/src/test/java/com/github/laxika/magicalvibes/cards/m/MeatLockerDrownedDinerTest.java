package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLUE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeatLockerDrownedDiner.class, GrizzlyBears.class})
class MeatLockerDrownedDinerTest extends BaseCardTest {

    @Test
    void meatLockerTapsAndPutsTwoStunCountersOnUpToOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRoom(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void meatLockerMayChooseNoTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRoom(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void drownedDinerDrawsThreeThenDiscardsOne() {
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);

        castRoom(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new MeatLockerDrownedDiner()));
        harness.addMana(player1, BLUE, doorIndex == 0 ? 3 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst()
                .orElseThrow();
    }
}
