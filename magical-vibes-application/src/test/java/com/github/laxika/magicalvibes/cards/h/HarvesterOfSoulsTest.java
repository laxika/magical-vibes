package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarvesterOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature dying offers a may-draw")
    void offersMayDrawOnNontokenDeath() {
        harness.addToBattlefield(player1, new HarvesterOfSouls());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithDoomBlade(bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Doom Blade was the only card in hand, so the drawn card is the only one left.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the may-draw draws nothing")
    void decliningDrawsNothing() {
        harness.addToBattlefield(player1, new HarvesterOfSouls());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithDoomBlade(bears);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A dying token creature does not trigger")
    void tokenDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarvesterOfSouls());
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCreature());

        killWithDoomBlade(token);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Harvester's own death does not trigger it")
    void ownDeathDoesNotTrigger() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new HarvesterOfSouls());

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, harvester.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Harvester of Souls");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void killWithDoomBlade(Permanent target) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities(); // resolve Doom Blade -> creature dies, trigger goes on the stack
        harness.passBothPriorities(); // resolve the triggered ability -> may prompt
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Saproling Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
