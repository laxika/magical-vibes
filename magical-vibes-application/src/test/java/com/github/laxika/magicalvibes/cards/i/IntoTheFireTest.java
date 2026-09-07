package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandraNalaar.class, DelugeOfTheDead.class, GrizzlyBears.class, HillGiant.class,
        InvasionOfInnistrad.class, IntoTheFire.class, Shock.class})
class IntoTheFireTest extends BaseCardTest {

    @Test
    void dealsDamageToCreaturesPlaneswalkersAndBattles() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);

        harness.setHand(player1, List.of(new IntoTheFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    void putsChosenCardsOnBottomThenDrawsOneAdditionalCard() {
        Shock keep = new Shock();
        GrizzlyBears bottomOne = new GrizzlyBears();
        HillGiant bottomTwo = new HillGiant();
        GrizzlyBears libraryOne = new GrizzlyBears();
        HillGiant libraryTwo = new HillGiant();
        Shock libraryThree = new Shock();
        GrizzlyBears libraryFour = new GrizzlyBears();
        harness.setHand(player1, List.of(new IntoTheFire(), keep, bottomOne, bottomTwo));
        harness.setLibrary(player1, List.of(libraryOne, libraryTwo, libraryThree, libraryFour));
        addMana();

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleMultipleCardsChosen(player1, List.of(bottomOne.getId(), bottomTwo.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(keep);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryFour, bottomOne, bottomTwo);
    }

    @Test
    void choosingZeroStillDrawsOneCard() {
        Shock keep = new Shock();
        harness.setHand(player1, List.of(new IntoTheFire(), keep));
        Shock draw = new Shock();
        harness.setLibrary(player1, List.of(draw));
        addMana();

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep, draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
