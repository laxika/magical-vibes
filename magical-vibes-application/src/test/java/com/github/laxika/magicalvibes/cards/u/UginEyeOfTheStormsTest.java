package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UginEyeOfTheStormsTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, Ugin exiles up to one colored permanent")
    void castTriggerExilesColoredPermanent() {
        Permanent colored = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent colorless = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        UginEyeOfTheStorms card = new UginEyeOfTheStorms();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castPlaneswalker(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, colorless.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, colored.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(colored.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(colorless);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }

    @Test
    @DisplayName("Whenever you cast a colorless spell, Ugin exiles up to one colored permanent")
    void colorlessSpellTriggerExilesColoredPermanent() {
        Permanent ugin = addReadyUgin(7);
        Permanent colored = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, colored.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(colored.getCard());
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Ornithopter);
    }

    @Test
    @DisplayName("+2 gains life and draws a card")
    void plusTwoGainsLifeAndDraws() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent ugin = addReadyUgin(7);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(9);
    }

    @Test
    @DisplayName("0 adds three colorless mana")
    void zeroAddsThreeColorlessMana() {
        Permanent ugin = addReadyUgin(7);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("−11 exiles matching cards and lets the controller cast them for free this turn")
    void ultimateExilesAndGrantsFreeCasting() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of(ornithopter, new GrizzlyBears(), new Forest()));
        Permanent ugin = addReadyUgin(11);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(ornithopter);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getCardsExiledByPermanent(ugin.getId())).containsExactly(ornithopter);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getClass)
                .containsExactlyInAnyOrder(GrizzlyBears.class, Forest.class);

        harness.castFromExile(player1, ornithopter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ornithopter.getId()));
    }

    private Permanent addReadyUgin(int loyalty) {
        Permanent ugin = new Permanent(new UginEyeOfTheStorms());
        ugin.setCounterCount(CounterType.LOYALTY, loyalty);
        ugin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ugin);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ugin;
    }
}
