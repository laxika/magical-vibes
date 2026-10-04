package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BoggartHarbinger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LysAlanaHuntmaster;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishHarbinger.class, LysAlanaHuntmaster.class, BoggartHarbinger.class,
        Plains.class, Island.class, ElvishPromenade.class})
class ElvishHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB may ability only offers Elf cards")
    void acceptingMayOffersOnlyElves() {
        setupAndCast();
        setupLibraryWithElves();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.ELF));
    }

    @Test
    @DisplayName("Choosing an Elf card puts it on top of the library")
    void choosingElfPutsItOnTop() {
        setupAndCast();
        setupLibraryWithElves();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithElves();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Mana ability adds exactly one mana of the chosen color")
    void manaAbilityAddsOneChosenColor() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new ElvishHarbinger());
            GameData gd = harness.getGameData();
            harbinger.setSummoningSick(false);
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            assertThat(harbinger.isTapped()).isTrue();
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    void canSearchForANoncreatureElfCard() {
        setupAndCast();
        Card promenade = new ElvishPromenade();
        Card island = new Island();
        harness.setLibrary(player1, List.of(island, promenade));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(promenade, island);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayFailToFindEvenWhenAnElfIsPresent() {
        setupAndCast();
        Card elf = new LysAlanaHuntmaster();
        Card island = new Island();
        harness.setLibrary(player1, List.of(elf, island));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(elf, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchWithNoElfFinishesWithoutChoosingACard() {
        setupAndCast();
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchWithAnEmptyLibraryFinishesNormally() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ElvishHarbinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void manaAbilityResolvesImmediatelyAndCannotProduceColorless() {
        Permanent harbinger = addCreatureReady(player1, new ElvishHarbinger());

        harness.activateAbility(player1, 0, null, null);

        assertThat(harbinger.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new ElvishHarbinger()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithElves() {
        harness.setLibrary(player1, List.of(new LysAlanaHuntmaster(), new BoggartHarbinger(), new Plains(), new Island()));
    }
}
