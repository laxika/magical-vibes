package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VisageOfBolasTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Visage of Bolas triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may finds Nicol Bolas, the Deceiver in graveyard and puts it into hand")
    void acceptingMayFindsInGraveyard() {
        Card bolas = createNicolBolasTheDeceiver();
        harness.setGraveyard(player1, List.of(bolas));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Nicol Bolas, the Deceiver");
        harness.assertNotInGraveyard(player1, "Nicol Bolas, the Deceiver");
    }

    @Test
    @DisplayName("Accepting may searches library when not in graveyard")
    void acceptingMaySearchesLibrary() {
        Card bolas = createNicolBolasTheDeceiver();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(bolas);
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .getFirst().getName()).isEqualTo("Nicol Bolas, the Deceiver");
    }

    @Test
    @DisplayName("Accepting may when Bolas is not in library or graveyard does nothing")
    void acceptingMayWhenNotFound() {
        gd.playerDecks.get(player1.getId()).clear();
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining may ability does not search")
    void decliningMayDoesNotSearch() {
        Card bolas = createNicolBolasTheDeceiver();
        harness.setGraveyard(player1, List.of(bolas));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Nicol Bolas, the Deceiver");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Visage of Bolas enters the battlefield after resolving")
    void visageOfBolasEntersBattlefield() {
        setupAndCast();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Visage of Bolas");
    }

    @Test
    @DisplayName("Activating the tap ability prompts a choice between blue, black and red")
    void activatingPromptsColorChoice() {
        harness.addToBattlefield(player1, new VisageOfBolas());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "BLACK", "RED");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"BLUE", "BLACK", "RED"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new VisageOfBolas());
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new VisageOfBolas()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
    }

    private Card createNicolBolasTheDeceiver() {
        Card bolas = new Card();
        bolas.setName("Nicol Bolas, the Deceiver");
        bolas.setType(CardType.PLANESWALKER);
        bolas.setManaCost("{4}{U}{B}{R}");
        return bolas;
    }
}
