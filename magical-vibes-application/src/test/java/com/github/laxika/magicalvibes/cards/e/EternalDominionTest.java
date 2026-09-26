package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.m.MikokoroCenterOfTheSea;
import com.github.laxika.magicalvibes.cards.r.Reverence;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EternalDominion.class, EbonyOwlNetsuke.class, ArabaMothrider.class, Reverence.class,
        MikokoroCenterOfTheSea.class, SpiritualVisit.class})
class EternalDominionTest extends BaseCardTest {

    @Test
    @DisplayName("Offers artifact, creature, enchantment, and land cards from the target library")
    void offersPermanentCardsFromTargetLibrary() {
        harness.setLibrary(player2, List.of(
                new EbonyOwlNetsuke(), new ArabaMothrider(), new Reverence(),
                new MikokoroCenterOfTheSea(), new SpiritualVisit()));
        castEternalDominion();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name")
                .containsExactlyInAnyOrder("Ebony Owl Netsuke", "Araba Mothrider", "Reverence",
                        "Mikokoro, Center of the Sea");
    }

    @Test
    @DisplayName("Puts the chosen card onto the battlefield under your control and applies Epic")
    void putsChosenCardUnderControlAndAppliesEpic() {
        harness.setLibrary(player2, List.of(new MikokoroCenterOfTheSea(), new SpiritualVisit()));
        castEternalDominion();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mikokoro, Center of the Sea");
        harness.assertNotOnBattlefield(player2, "Mikokoro, Center of the Sea");
        assertThat(gd.playerDecks.get(player2.getId())).extracting("name").containsExactly("Spiritual Visit");

        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Copies the spell at each upkeep and keeps the original opponent target when declined")
    void copiesSpellAtEachUpkeep() {
        harness.setLibrary(player2, List.of(new MikokoroCenterOfTheSea(), new SpiritualVisit()));
        castEternalDominion();
        chooseLibraryCard("Mikokoro, Center of the Sea");

        harness.setLibrary(player2, List.of(new EbonyOwlNetsuke(), new SpiritualVisit()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name").containsExactly("Ebony Owl Netsuke");

        chooseLibraryCard("Ebony Owl Netsuke");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactlyInAnyOrder("Mikokoro, Center of the Sea", "Ebony Owl Netsuke");
    }

    @Test
    @DisplayName("Still applies Epic when the target library has no matching card")
    void appliesEpicWhenNoMatchingCardIsFound() {
        harness.setLibrary(player2, List.of(new SpiritualVisit()));
        castEternalDominion();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).extracting("name")
                .containsExactly("Spiritual Visit");

        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new EternalDominion()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEternalDominion() {
        harness.setHand(player1, List.of(new EternalDominion()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void chooseLibraryCard(String cardName) {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int cardIndex = search.params().cards().stream()
                .map(card -> card.getName())
                .toList()
                .indexOf(cardName);
        assertThat(cardIndex).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, cardIndex);
    }
}
