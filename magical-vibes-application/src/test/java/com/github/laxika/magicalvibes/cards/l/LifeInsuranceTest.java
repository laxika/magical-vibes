package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeInsurance.class, GrizzlyBears.class, Shock.class})
class LifeInsuranceTest extends BaseCardTest {

    @Test
    @DisplayName("Paying extort drains an opponent and gains life")
    void payingExtortDrainsOpponentAndGainsLife() {
        harness.addToBattlefield(player1, new LifeInsurance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A nontoken creature dying causes life loss and creates a Treasure")
    void nontokenCreatureDeathLosesLifeAndCreatesTreasure() {
        harness.addToBattlefield(player1, new LifeInsurance());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("A token creature death does not trigger Life Insurance")
    void tokenCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new LifeInsurance());
        Card token = new GrizzlyBears();
        token.setToken(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, token);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
