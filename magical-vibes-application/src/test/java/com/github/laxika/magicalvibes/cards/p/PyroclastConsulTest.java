package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyroclastConsulTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing the shared-type card deals 2 damage to each creature on both sides")
    void revealDealsDamageToEachCreature() {
        addCreatureReady(player1, new PyroclastConsul());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setLibraryTop(new PyroclastConsul()); // Elemental Shaman — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // 2/2 Grizzly Bears die on both sides; the 3/3 Consul survives.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Pyroclast Consul");
    }

    @Test
    @DisplayName("Declining to reveal deals no damage")
    void decliningDealsNoDamage() {
        addCreatureReady(player1, new PyroclastConsul());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setLibraryTop(new PyroclastConsul());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new PyroclastConsul());
        setLibraryTop(new GrizzlyBears()); // Bear — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void setLibraryTop(Card card) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(card);
    }
}
