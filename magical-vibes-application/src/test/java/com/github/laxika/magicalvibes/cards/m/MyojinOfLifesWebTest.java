package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfLifesWeb.class, HumbleBudoka.class, Forest.class, SoulWarden.class})
class MyojinOfLifesWebTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with a divinity counter and indestructible")
    void castFromHandEntersWithDivinityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new MyojinOfLifesWeb(), "{6}{G}{G}{G}");
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Life's Web");
        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get a divinity counter")
    void enteringWithoutCastingDoesNotGetDivinityCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfLifesWeb());

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing the divinity counter puts any number of creatures from hand onto the battlefield untapped")
    void putsCreaturesUntilDeclined() {
        Permanent myojin = addReadyMyojin(player1);
        Card creatureOne = new HumbleBudoka();
        Card creatureTwo = new HumbleBudoka();
        Card forest = new Forest();
        harness.setHand(player1, List.of(creatureOne, creatureTwo, forest));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();

        var choice = (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactlyInAnyOrder(0, 1);
        assertThat(choice.putAnyNumber()).isTrue();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Humble Budoka"))
                .hasSize(2)
                .allMatch(p -> !p.isTapped());
    }

    @Test
    @DisplayName("Declining the choice puts no creatures onto the battlefield")
    void decliningPutsNone() {
        addReadyMyojin(player1);
        Card creature = new HumbleBudoka();
        harness.setHand(player1, List.of(creature));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("The ability only puts creatures from its controller's hand onto the battlefield")
    void onlyUsesControllerHand() {
        addReadyMyojin(player1);
        Card forest = new Forest();
        Card opponentCreature = new HumbleBudoka();
        harness.setHand(player1, List.of(forest));
        harness.setHand(player2, List.of(opponentCreature));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player2, "Humble Budoka");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCreature);
    }

    @Test
    @DisplayName("The ability cannot be activated without a divinity counter")
    void cannotActivateWithoutDivinityCounter() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setCounterCount(CounterType.DIVINITY, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("All chosen creatures enter simultaneously and see each other enter")
    void chosenCreaturesSeeEachOtherEnter() {
        addReadyMyojin(player1);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SoulWarden(), new SoulWarden()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The controller may stop after putting only some eligible creatures onto the battlefield")
    void canChooseOnlySomeCreatures() {
        addReadyMyojin(player1);
        Card chosen = new HumbleBudoka();
        Card remaining = new HumbleBudoka();
        harness.setHand(player1, List.of(chosen, remaining));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == chosen)
                .hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick tapped Myojin can activate during an opponent's turn and pays its counter immediately")
    void activatesWithoutTappingAtInstantSpeed() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setSummoningSick(true);
        myojin.tap();
        harness.setHand(player1, List.of(new HumbleBudoka()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(myojin.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Humble Budoka");
    }

    private Permanent addReadyMyojin(Player player) {
        Permanent myojin = harness.addToBattlefieldAndReturn(player, new MyojinOfLifesWeb());
        myojin.setSummoningSick(false);
        myojin.setCounterCount(CounterType.DIVINITY, 1);
        return myojin;
    }
}
