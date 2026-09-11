package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({RepositorySkaab.class, GrizzlyBears.class, Shock.class, Divination.class})
class RepositorySkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not return a spell card")
    void decliningExploitDoesNothing() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Repository Skaab");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Exploit sacrifices a creature and returns a target instant to hand")
    void exploitReturnsInstantToHand() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Repository Skaab");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Exploit can return a target sorcery to hand")
    void exploitReturnsSorceryToHand() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Exploit trigger cannot target a creature card")
    void exploitCannotTargetCreatureCard() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castRepositorySkaab() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RepositorySkaab()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
