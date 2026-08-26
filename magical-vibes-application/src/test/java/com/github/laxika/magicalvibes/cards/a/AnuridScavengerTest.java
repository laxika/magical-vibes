package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnuridScavenger.class, DarkBanishing.class, GrizzlyBears.class, HillGiant.class})
class AnuridScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a card from its controller's graveyard on the library bottom keeps it")
    void putsOwnGraveyardCardOnLibraryBottom() {
        Permanent scavenger = addCreatureReady(player1, new AnuridScavenger());
        Card graveyardCard = new GrizzlyBears();
        Card opponentGraveyardCard = new HillGiant();
        Card libraryCard = new HillGiant();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.setLibrary(player1, new ArrayList<>(List.of(libraryCard)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scavenger);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, graveyardCard);
    }

    @Test
    @DisplayName("Declining the payment sacrifices Anurid Scavenger")
    void decliningPaymentSacrificesIt() {
        Permanent scavenger = addCreatureReady(player1, new AnuridScavenger());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scavenger);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard, scavenger.getCard());
    }

    @Test
    @DisplayName("An empty graveyard causes Anurid Scavenger to be sacrificed")
    void emptyGraveyardSacrificesIt() {
        Permanent scavenger = addCreatureReady(player1, new AnuridScavenger());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scavenger);
    }

    @Test
    @DisplayName("Protection from black prevents black spells from targeting it")
    void protectionFromBlackPreventsTargeting() {
        Permanent scavenger = harness.addToBattlefieldAndReturn(player2, new AnuridScavenger());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, scavenger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
