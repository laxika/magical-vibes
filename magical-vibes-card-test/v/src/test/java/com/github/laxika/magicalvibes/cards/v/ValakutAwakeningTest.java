package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AkoumHellhound;
import com.github.laxika.magicalvibes.cards.g.GrotagBugCatcher;
import com.github.laxika.magicalvibes.cards.i.InordinateRage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValakutAwakening.class, ValakutStoneforge.class, AkoumHellhound.class, GrotagBugCatcher.class,
        InordinateRage.class})
class ValakutAwakeningTest extends BaseCardTest {

    @Test
    void putsChosenCardsOnBottomThenDrawsOneAdditionalCard() {
        InordinateRage keep = new InordinateRage();
        AkoumHellhound bottomOne = new AkoumHellhound();
        GrotagBugCatcher bottomTwo = new GrotagBugCatcher();
        AkoumHellhound libraryOne = new AkoumHellhound();
        GrotagBugCatcher libraryTwo = new GrotagBugCatcher();
        InordinateRage libraryThree = new InordinateRage();
        AkoumHellhound libraryFour = new AkoumHellhound();
        harness.setHand(player1, List.of(new ValakutAwakening(), keep, bottomOne, bottomTwo));
        harness.setLibrary(player1, List.of(libraryOne, libraryTwo, libraryThree, libraryFour));
        addMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleMultipleCardsChosen(player1, List.of(bottomOne.getId(), bottomTwo.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep, libraryOne, libraryTwo, libraryThree);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryFour, bottomOne, bottomTwo);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosingZeroStillDrawsOneCard() {
        InordinateRage keep = new InordinateRage();
        harness.setHand(player1, List.of(new ValakutAwakening(), keep));
        InordinateRage draw = new InordinateRage();
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

    @Test
    void emptyHandAfterCastingStillDrawsOneCard() {
        InordinateRage draw = new InordinateRage();
        harness.setLibrary(player1, List.of(draw));
        harness.castFromHand(player1, new ValakutAwakening(), "{2}{R}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBottomEntireHandAndRedrawBottomedCardsInChosenOrder() {
        AkoumHellhound first = new AkoumHellhound();
        GrotagBugCatcher second = new GrotagBugCatcher();
        InordinateRage top = new InordinateRage();
        harness.setHand(player1, List.of(new ValakutAwakening(), first, second));
        harness.setLibrary(player1, List.of(top));
        addMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, second, first);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landFaceUsesTheNormalLandPlayAllowance() {
        harness.setHand(player1, List.of(new ValakutAwakening(), new ValakutAwakening()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
