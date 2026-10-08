package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClackbridgeTroll.class, Gingerbrute.class})
class ClackbridgeTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and gives the targeted opponent three Goat tokens")
    void entersAndCreatesGoatsForTargetOpponent() {
        harness.setHand(player1, new ArrayList<>(List.of(new ClackbridgeTroll())));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> goats = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Goat"))
                .toList();
        assertThat(goats).hasSize(3);
        assertThat(goats).allSatisfy(goat -> {
            assertThat(goat.getCard().getPower()).isEqualTo(0);
            assertThat(goat.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Declining the combat choice leaves the Troll and opponent creature unchanged")
    void decliningDoesNothing() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player2, new Gingerbrute());
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>());

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(troll.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Gingerbrute");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting sacrifices a creature, taps the Troll, gains life, and draws")
    void acceptingSacrificesAndRewardsController() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player2, new Gingerbrute());
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Gingerbrute()));

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(troll.isTapped()).isTrue();
        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player2, "Gingerbrute");
        harness.assertInHand(player1, "Gingerbrute");
    }

    @Test
    @DisplayName("Accepting with several creatures asks the opponent which one to sacrifice")
    void acceptingWithSeveralCreaturesAsksWhichOne() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player2, new Gingerbrute());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Gingerbrute()));

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(troll.isTapped()).isTrue();
        harness.assertLife(player1, 23);
        harness.assertInHand(player1, "Gingerbrute");
    }

    @Test
    @DisplayName("Triggers only at the beginning of combat on its controller's turn")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player2, new Gingerbrute());

        resolveBeginningOfCombat(player2);

        assertThat(gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Sacrificing still rewards the controller when the Troll is already tapped")
    void alreadyTappedTrollStillRewardsController() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        troll.tap();
        harness.addToBattlefield(player2, new Gingerbrute());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Gingerbrute()));

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(troll.isTapped()).isTrue();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Gingerbrute");
        harness.assertInHand(player1, "Gingerbrute");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent with no creatures cannot sacrifice the controller's creature")
    void noOpponentCreaturesMeansNoReward() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ClackbridgeTroll());
        harness.addToBattlefield(player1, new Gingerbrute());
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Gingerbrute()));

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(troll.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Gingerbrute");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
