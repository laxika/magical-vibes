package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PersuasiveInterrogators.class})
class PersuasiveInterrogatorsTest extends BaseCardTest {

    @Test
    void investigatesWhenItEnters() {
        harness.setHand(player1, List.of(new PersuasiveInterrogators()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificingAClueGivesTargetOpponentTwoPoisonCounters() {
        harness.addToBattlefield(player1, new PersuasiveInterrogators());
        Permanent clue = addClueToken(player1);
        harness.setLibrary(player1, List.of(new PersuasiveInterrogators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void cannotChooseTheControllerAsTheTarget() {
        harness.addToBattlefield(player1, new PersuasiveInterrogators());
        Permanent clue = addClueToken(player1);
        harness.setLibrary(player1, List.of(new PersuasiveInterrogators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createdClueDrawsACardAndTriggersPoisonBeforeTheDrawResolves() {
        harness.enterBattlefieldAndReturn(player1, new PersuasiveInterrogators());
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PersuasiveInterrogators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent clue = findPermanent(player1, "Clue");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        harness.assertInHand(player1, "Persuasive Interrogators");
    }

    @Test
    void opponentsClueSacrificeDoesNotTriggerYourInterrogators() {
        harness.addToBattlefield(player1, new PersuasiveInterrogators());
        Permanent opponentInterrogators = harness.enterBattlefieldAndReturn(player2, new PersuasiveInterrogators());
        resolveAllTriggers();
        gd.playerBattlefields.get(player2.getId()).remove(opponentInterrogators);
        harness.setLibrary(player2, List.of(new PersuasiveInterrogators()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        Permanent clue = findPermanent(player2, "Clue");
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInHand(player2, "Persuasive Interrogators");
    }

    @Test
    void eachInterrogatorTriggersForTheSameClueSacrifice() {
        harness.enterBattlefieldAndReturn(player1, new PersuasiveInterrogators());
        resolveAllTriggers();
        harness.addToBattlefield(player1, new PersuasiveInterrogators());
        harness.setLibrary(player1, List.of(new PersuasiveInterrogators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent clue = findPermanent(player1, "Clue");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        harness.assertInHand(player1, "Persuasive Interrogators");
    }

    @Test
    void triggersForEveryClueSacrificedInTheSameTurn() {
        harness.enterBattlefieldAndReturn(player1, new PersuasiveInterrogators());
        resolveAllTriggers();
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new PersuasiveInterrogators(), new PersuasiveInterrogators()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 2; i++) {
            Permanent clue = findPermanent(player1, "Clue");
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
            resolveAllTriggers();
            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();
        }

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Clue");
    }

    @Test
    void poisonTriggerResolvesAfterInterrogatorsLeaveTheBattlefield() {
        Permanent interrogators = harness.enterBattlefieldAndReturn(player1, new PersuasiveInterrogators());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new PersuasiveInterrogators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent clue = findPermanent(player1, "Clue");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(interrogators);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        harness.assertInHand(player1, "Persuasive Interrogators");
    }

    @Test
    void opponentLosesWhenTheTriggerBringsPoisonToTen() {
        harness.enterBattlefieldAndReturn(player1, new PersuasiveInterrogators());
        resolveAllTriggers();
        gd.playerPoisonCounters.put(player2.getId(), 8);
        harness.setLibrary(player1, List.of(new PersuasiveInterrogators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent clue = findPermanent(player1, "Clue");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private Permanent addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        return harness.addToBattlefieldAndReturn(player, clueCard);
    }
}
