package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DrownerOfHope;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FromBeyond.class, DrownerOfHope.class, OranRiefInvoker.class})
class FromBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Eldrazi Scion at the beginning of your upkeep")
    void createsEldraziScionOnUpkeep() {
        harness.addToBattlefield(player1, new FromBeyond());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("The Eldrazi Scion can be sacrificed for colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        harness.addToBattlefield(player1, new FromBeyond());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing From Beyond searches for an Eldrazi card")
    void sacrificesToSearchForEldrazi() {
        harness.addToBattlefield(player1, new FromBeyond());
        DrownerOfHope eldrazi = new DrownerOfHope();
        harness.setLibrary(player1, List.of(new OranRiefInvoker(), eldrazi));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(eldrazi);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Drowner of Hope");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not create a Scion during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new FromBeyond());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(findPermanents(player2, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("May fail to find even when an Eldrazi is in the library")
    void mayFailToFindEldrazi() {
        harness.addToBattlefield(player1, new FromBeyond());
        DrownerOfHope eldrazi = new DrownerOfHope();
        harness.setLibrary(player1, List.of(eldrazi));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eldrazi);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "From Beyond");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library still sacrifices From Beyond")
    void searchesEmptyLibrary() {
        harness.addToBattlefield(player1, new FromBeyond());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "From Beyond");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creates an untapped colorless 1/1 Eldrazi Scion creature")
    void createdScionHasCorrectCharacteristics() {
        harness.addToBattlefield(player1, new FromBeyond());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        assertThat(scion.isTapped()).isFalse();
        assertThat(scion.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gqs.getEffectiveColors(gd, scion)).isEmpty();
        assertThat(scion.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays mana and sacrifices From Beyond before the search resolves")
    void paysCostsBeforeSearchResolves() {
        harness.addToBattlefield(player1, new FromBeyond());
        DrownerOfHope eldrazi = new DrownerOfHope();
        harness.setLibrary(player1, List.of(eldrazi));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "From Beyond");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eldrazi);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Drowner of Hope");
    }
}
