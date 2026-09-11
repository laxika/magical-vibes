package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValakutAwakening.class, ValakutStoneforge.class, GrizzlyBears.class, HillGiant.class,
        Shock.class})
class ValakutAwakeningTest extends BaseCardTest {

    @Test
    void putsChosenCardsOnBottomThenDrawsOneAdditionalCard() {
        Shock keep = new Shock();
        GrizzlyBears bottomOne = new GrizzlyBears();
        HillGiant bottomTwo = new HillGiant();
        GrizzlyBears libraryOne = new GrizzlyBears();
        HillGiant libraryTwo = new HillGiant();
        Shock libraryThree = new Shock();
        GrizzlyBears libraryFour = new GrizzlyBears();
        harness.setHand(player1, List.of(new ValakutAwakening(), keep, bottomOne, bottomTwo));
        harness.setLibrary(player1, List.of(libraryOne, libraryTwo, libraryThree, libraryFour));
        addMana();

        harness.castModalInstant(player1, 0, 0, List.of());
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
        harness.setHand(player1, List.of(new ValakutAwakening(), keep));
        Shock draw = new Shock();
        harness.setLibrary(player1, List.of(draw));
        addMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep, draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void landFaceEntersTappedAndProducesRedMana() {
        ValakutAwakening card = new ValakutAwakening();
        harness.setHand(player1, List.of(card));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent stoneforge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(stoneforge.getCard()).isInstanceOf(ValakutStoneforge.class);
        assertThat(stoneforge.isTapped()).isTrue();

        stoneforge.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
