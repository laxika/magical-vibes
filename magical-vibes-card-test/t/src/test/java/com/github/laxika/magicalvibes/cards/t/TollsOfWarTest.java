package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TollsOfWar.class, Plains.class})
class TollsOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Clue token when it enters")
    void createsClueOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new TollsOfWar());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Creates an Ally when you sacrifice a permanent during your turn")
    void createsAllyOnSacrificeDuringYourTurn() {
        harness.addToBattlefield(player1, new TollsOfWar());
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();

        Permanent ally = findPermanents(player1, "Ally").getFirst();
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new TollsOfWar());
        addClueToken(player1);
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ally")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger during another player's turn")
    void doesNotTriggerDuringAnotherPlayersTurn() {
        harness.addToBattlefield(player1, new TollsOfWar());
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new Plains()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    @Test
    void generatedClueCanBeSacrificedToDrawAndCreateAnAlly() {
        harness.enterBattlefieldAndReturn(player1, new TollsOfWar());
        resolveAllTriggers();
        Plains drawnCard = new Plains();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(findPermanents(player1, "Ally")).hasSize(1);
    }

    @Test
    void opponentsSacrificeDoesNotTriggerYourEnchantment() {
        harness.addToBattlefield(player1, new TollsOfWar());
        harness.enterBattlefieldAndReturn(player2, new TollsOfWar());
        resolveAllTriggers();
        harness.setLibrary(player2, List.of(new Plains()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);

        harness.activateAbility(player2, permanentIndex(player2, "Clue"), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ally")).isEmpty();
        assertThat(findPermanents(player2, "Ally")).isEmpty();
    }

    @Test
    void eachCopyTriggersIndependently() {
        harness.enterBattlefieldAndReturn(player1, new TollsOfWar());
        harness.enterBattlefieldAndReturn(player1, new TollsOfWar());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Ally")).hasSize(2);

        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Ally")).hasSize(2);
    }

    @Test
    @CardUsed(ClawsOfGix.class)
    void sacrificingTheEnchantmentTriggersItsOwnAbility() {
        harness.addToBattlefield(player1, new ClawsOfGix());
        Permanent tolls = harness.addToBattlefieldAndReturn(player1, new TollsOfWar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, tolls.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tolls of War");
        assertThat(findPermanents(player1, "Ally")).hasSize(1);
    }

    @Test
    void canTriggerAgainOnYourNextTurn() {
        harness.enterBattlefieldAndReturn(player1, new TollsOfWar());
        resolveAllTriggers();
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ally")).hasSize(2);
    }

    @Test
    @CardUsed(ClawsOfGix.class)
    void sacrificingTheEnchantmentAfterItTriggeredDoesNotTriggerAgain() {
        harness.addToBattlefield(player1, new ClawsOfGix());
        Permanent tolls = harness.enterBattlefieldAndReturn(player1, new TollsOfWar());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, permanentIndex(player1, "Clue"), null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Ally")).hasSize(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, tolls.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tolls of War");
        assertThat(findPermanents(player1, "Ally")).hasSize(1);
    }

    private int permanentIndex(Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }

    private void addClueToken(Player player) {
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
        Permanent clue = harness.addToBattlefieldAndReturn(player, clueCard);
        clue.setSummoningSick(false);
    }
}
