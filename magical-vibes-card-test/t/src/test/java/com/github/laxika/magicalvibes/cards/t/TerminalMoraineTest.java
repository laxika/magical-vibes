package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerminalMoraine.class, Forest.class, Plains.class, GrizzlyBears.class})
class TerminalMoraineTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds colorless mana")
    void manaAbilityAddsColorlessMana() {
        harness.addToBattlefield(player1, new TerminalMoraine());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Search ability sacrifices Terminal Moraine and puts a basic land onto the battlefield tapped")
    void searchesForBasicLandToBattlefieldTapped() {
        harness.addToBattlefield(player1, new TerminalMoraine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Terminal Moraine");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND))
                .allMatch(card -> card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    @DisplayName("Search ability may fail to find a basic land")
    void mayFailToFindBasicLand() {
        GrizzlyBears nonBasicCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new TerminalMoraine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(nonBasicCard));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Terminal Moraine");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasicCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest || permanent.getCard() instanceof Plains);
    }

    @Test
    @DisplayName("May decline to find a basic land even when one is available")
    void mayDeclineAvailableBasicLand() {
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new TerminalMoraine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertInGraveyard(player1, "Terminal Moraine");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Search excludes nonbasic lands and removes the chosen basic land from the library")
    void excludesNonbasicLands() {
        TerminalMoraine nonbasicLand = new TerminalMoraine();
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new TerminalMoraine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(nonbasicLand, forest));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Search cannot be activated with only one mana")
    void requiresTwoMana() {
        harness.addToBattlefield(player1, new TerminalMoraine());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Terminal Moraine");
        harness.assertNotInGraveyard(player1, "Terminal Moraine");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Terminal Moraine cannot activate its search ability")
    void tappedLandCannotSearch() {
        harness.addToBattlefield(player1, new TerminalMoraine());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Terminal Moraine");
        harness.assertNotInGraveyard(player1, "Terminal Moraine");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
