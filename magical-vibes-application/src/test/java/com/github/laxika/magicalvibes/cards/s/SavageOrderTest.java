package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SavageOrder.class, ColossalDreadmaw.class, FrenziedRaptor.class, GrizzlyBears.class})
class SavageOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature with power 4 or greater and puts a Dinosaur onto the battlefield")
    void sacrificesLargeCreatureAndFindsDinosaur() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Card dinosaur = new FrenziedRaptor();
        harness.setLibrary(player1, List.of(dinosaur));
        castSavageOrder(sacrifice);

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(dinosaur);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
        Permanent found = findPermanent(player1, "Frenzied Raptor");
        assertThat(found).isNotNull();
        assertThat(found.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The fetched Dinosaur keeps indestructible through cleanup and loses it on the next turn")
    void indestructibleLastsUntilNextTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new FrenziedRaptor()));
        castSavageOrder(sacrifice);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent found = findPermanent(player1, "Frenzied Raptor");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(found.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        endTurn(player1);
        endTurn(player2);
        assertThat(found.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot cast without a creature with power 4 or greater to sacrifice")
    void requiresLargeCreatureSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageOrder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    private void castSavageOrder(Permanent sacrifice) {
        harness.setHand(player1, List.of(new SavageOrder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
