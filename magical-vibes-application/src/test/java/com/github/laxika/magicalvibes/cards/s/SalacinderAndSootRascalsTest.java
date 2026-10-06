package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.cards.t.TakeInventory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SalacinderAndSootRascals.class, AirElemental.class, GrizzlyBears.class,
        RiteOfFlame.class, TakeInventory.class})
class SalacinderAndSootRascalsTest extends BaseCardTest {

    @Test
    void entersWithAChoiceBetweenRiteOfFlameAndTakeInventory() {
        harness.enterBattlefieldAndReturn(player1, new SalacinderAndSootRascals());
        harness.passBothPriorities();

        chooseSpellbookCard("Rite of Flame");

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Rite of Flame");
    }

    @Test
    void triggersWhenControllerCastsAnElementalSpell() {
        harness.addToBattlefield(player1, new SalacinderAndSootRascals());
        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();

        chooseSpellbookCard("Take Inventory");

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Take Inventory");
    }

    @Test
    void doesNotTriggerForANonElementalSpell() {
        harness.addToBattlefield(player1, new SalacinderAndSootRascals());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class))
                .isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void entryRequiresModeChoiceBeforeEitherPlayerCanRespond() {
        harness.enterBattlefieldAndReturn(player1, new SalacinderAndSootRascals());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void elementalCastRequiresModeChoiceBeforeTriggerResolution() {
        harness.addToBattlefield(player1, new SalacinderAndSootRascals());
        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsElementalDoesNotTrigger() {
        harness.addToBattlefield(player1, new SalacinderAndSootRascals());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void noncreatureSpellGivesProwessWithoutConjuring() {
        var rascals = harness.addToBattlefieldAndReturn(player1, new SalacinderAndSootRascals());
        harness.castFromHand(player1, new RiteOfFlame(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rascals)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rascals)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, rascals)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rascals)).isEqualTo(3);
    }

    @Test
    void tramplesOverABear() {
        addCreatureReady(player1, new SalacinderAndSootRascals());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Salacinder and Soot, Rascals");
    }

    private void chooseSpellbookCard(String cardName) {
        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice).isNotNull();

        var selectedCard = choice.cards().stream()
                .filter(card -> card.getName().equals(cardName))
                .findFirst()
                .orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(selectedCard.getId()));
    }
}
