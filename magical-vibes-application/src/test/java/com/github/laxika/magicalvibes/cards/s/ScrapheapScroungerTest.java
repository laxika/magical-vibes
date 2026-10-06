package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrapheapScrounger.class, SyndicateTrafficker.class})
class ScrapheapScroungerTest extends BaseCardTest {

    @Test
    @DisplayName("The graveyard ability returns Scrapheap Scrounger by exiling another creature card")
    void graveyardAbilityReturnsSelf() {
        ScrapheapScrounger scrounger = new ScrapheapScrounger();
        harness.setGraveyard(player1, List.of(new SyndicateTrafficker(), scrounger));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scrapheap Scrounger");
        harness.assertNotInGraveyard(player1, "Scrapheap Scrounger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Syndicate Trafficker");
    }

    @Test
    @DisplayName("The graveyard ability requires another creature card to exile")
    void graveyardAbilityRequiresAnotherCreature() {
        harness.setGraveyard(player1, List.of(new ScrapheapScrounger()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard to exile");

        harness.assertInGraveyard(player1, "Scrapheap Scrounger");
    }

    @Test
    @DisplayName("Scrapheap Scrounger cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent scrounger = harness.addToBattlefieldAndReturn(player2, new ScrapheapScrounger());
        scrounger.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SyndicateTrafficker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Another Scrounger can pay the cost, and exile happens before resolution")
    void anotherCopyPaysCostBeforeResolution() {
        ScrapheapScrounger source = new ScrapheapScrounger();
        ScrapheapScrounger payment = new ScrapheapScrounger();
        harness.setGraveyard(player1, List.of(source, payment));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(payment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        harness.assertNotOnBattlefield(player1, "Scrapheap Scrounger");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature in the opponent's graveyard cannot pay the exile cost")
    void cannotExileOpponentsCreature() {
        ScrapheapScrounger source = new ScrapheapScrounger();
        ScrapheapScrounger opponentCard = new ScrapheapScrounger();
        harness.setGraveyard(player1, List.of(source));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard to exile");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The exile cost is not paid when the black mana is missing")
    void missingBlackManaDoesNotExileCreature() {
        ScrapheapScrounger source = new ScrapheapScrounger();
        ScrapheapScrounger payment = new ScrapheapScrounger();
        harness.setGraveyard(player1, List.of(source, payment));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source, payment);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An older activation cannot return a Scrounger that has left the graveyard and died again")
    void olderActivationCannotReturnNewGraveyardObject() {
        ScrapheapScrounger source = new ScrapheapScrounger();
        ScrapheapScrounger firstPayment = new ScrapheapScrounger();
        SyndicateTrafficker secondPayment = new SyndicateTrafficker();
        harness.setGraveyard(player1, List.of(source, firstPayment, secondPayment));
        harness.addToBattlefield(player1, new SyndicateTrafficker());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstPayment.getId()));
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Scrapheap Scrounger");
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Scrapheap Scrounger");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scrapheap Scrounger");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstPayment, secondPayment);
    }
}
