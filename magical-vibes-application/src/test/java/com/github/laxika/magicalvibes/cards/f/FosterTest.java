package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FosterTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} puts the first revealed creature into hand and the rest into the graveyard")
    void payingFindsCreatureAndMillsTheRest() {
        Card shock = new Shock();
        Card island = new Island();
        Card creature = new GrizzlyBears();
        Card deadCreature = prepareDeathTrigger(List.of(shock, island, creature), true);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(deadCreature, shock, island);
    }

    @Test
    @DisplayName("Declining Foster's trigger leaves the library unchanged")
    void decliningLeavesLibraryUnchanged() {
        Card shock = new Shock();
        Card creature = new GrizzlyBears();
        prepareDeathTrigger(List.of(shock, creature), false);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock, creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Paying with no creature in the library puts every revealed card into the graveyard")
    void noCreatureMillsTheEntireLibrary() {
        Card shock = new Shock();
        Card island = new Island();
        Card deadCreature = prepareDeathTrigger(List.of(shock, island), true);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(deadCreature, shock, island);
    }

    private Card prepareDeathTrigger(List<Card> library, boolean addPaymentMana) {
        harness.addToBattlefield(player1, new Foster());
        Card deadCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, deadCreature);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        if (addPaymentMana) {
            harness.addMana(player1, ManaColor.COLORLESS, 1);
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        return deadCreature;
    }
}
