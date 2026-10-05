package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlanarBridge.class, Forest.class, GrizzlyBears.class, Shock.class})
class PlanarBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a permanent and puts it onto the battlefield")
    void searchesForPermanent() {
        harness.addToBattlefield(player1, new PlanarBridge());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getType)
                .containsExactlyInAnyOrder(CardType.LAND, CardType.CREATURE);

        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without eight mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new PlanarBridge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void activationPaysTapCostBeforeResolving() {
        var bridge = harness.addToBattlefieldAndReturn(player1, new PlanarBridge());
        harness.addMana(player1, ManaColor.COLORLESS, 16);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(bridge.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canFailToFindEvenWithPermanentInLibrary() {
        harness.addToBattlefield(player1, new PlanarBridge());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        harness.addToBattlefield(player1, new PlanarBridge());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutFindingAnInstant() {
        harness.addToBattlefield(player1, new PlanarBridge());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
