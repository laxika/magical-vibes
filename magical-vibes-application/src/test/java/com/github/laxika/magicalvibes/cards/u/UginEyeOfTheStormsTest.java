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
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UginEyeOfTheStorms.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
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

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(ugin.getId())).containsExactly(ornithopter);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getClass)
                .containsExactlyInAnyOrder(GrizzlyBears.class, Forest.class);

        harness.castFromExile(player1, ornithopter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ornithopter.getId()));
    }

    @Test
    void castTriggerCanResolveWithoutColoredPermanents() {
        UginEyeOfTheStorms card = new UginEyeOfTheStorms();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }

    @Test
    void ultimateCastsSevenManaCardForFreeAfterSourceDies() {
        UginEyeOfTheStorms selected = new UginEyeOfTheStorms();
        harness.setLibrary(player1, List.of(selected));
        Permanent source = addReadyUgin(11);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);

        harness.castFromExile(player1, selected.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(selected.getId()));
    }

    @Test
    void ultimateAllowsCastingEverySelectedCard() {
        Ornithopter first = new Ornithopter();
        Ornithopter second = new Ornithopter();
        harness.setLibrary(player1, List.of(first, second));
        addReadyUgin(11);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
    }

    @Test
    void laterUltimateDoesNotAllowCastingCardsFromEarlierSearch() {
        Ornithopter previouslyExiled = new Ornithopter();
        harness.setLibrary(player1, List.of(previouslyExiled, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        addReadyUgin(23);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, previouslyExiled.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, previouslyExiled.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ultimateCanFindNoCards() {
        Ornithopter card = new Ornithopter();
        harness.setLibrary(player1, List.of(card));
        addReadyUgin(11);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void coloredSpellDoesNotTriggerExile() {
        addReadyUgin(7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
    }

    @Test
    void zeroAbilityUsesTheStack() {
        addReadyUgin(7);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    private Permanent addReadyUgin(int loyalty) {
        Permanent ugin = harness.addToBattlefieldAndReturn(player1, new UginEyeOfTheStorms());
        ugin.setCounterCount(CounterType.LOYALTY, loyalty);
        ugin.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ugin;
    }
}
