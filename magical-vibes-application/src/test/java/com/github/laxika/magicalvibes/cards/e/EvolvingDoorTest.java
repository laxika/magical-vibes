package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NayaSojourners;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvolvingDoor.class, GrizzlyBears.class, NayaSojourners.class, Ornithopter.class,
        SelesnyaGuildmage.class})
class EvolvingDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a creature with exactly one more color than the sacrificed creature")
    void searchesForOneMoreColor() {
        activateDoor(new SelesnyaGuildmage(), new NayaSojourners(), new GrizzlyBears(),
                new SelesnyaGuildmage());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getName)
                .containsExactly("Naya Sojourners");
    }

    @Test
    @DisplayName("Offers the found creature for a normal-cost cast")
    void offersNormalCostCast() {
        GrizzlyBears bears = new GrizzlyBears();
        activateDoor(new Ornithopter(), bears);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void activateDoor(Card sacrificed, Card... library) {
        harness.addToBattlefield(player1, new EvolvingDoor());
        addCreatureReady(player1, sacrificed);
        harness.setLibrary(player1, List.of(library));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
