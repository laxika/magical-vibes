package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThrummingStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScryingSheets.class, SnowCoveredForest.class, ThrummingStone.class})
class ScryingSheetsTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        Permanent sheets = harness.addToBattlefieldAndReturn(player1, new ScryingSheets());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(sheets.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a snow top card into hand when the reveal is accepted")
    void acceptsSnowTopCard() {
        Permanent sheets = harness.addToBattlefieldAndReturn(player1, new ScryingSheets());
        Card topCard = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(topCard, new ThrummingStone()));
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(sheets.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Declining the reveal leaves a snow top card on the library")
    void declinesSnowTopCard() {
        harness.addToBattlefield(player1, new ScryingSheets());
        Card topCard = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(topCard, new ThrummingStone()));
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("A nonsnow top card stays on top without offering a choice")
    void nonSnowTopCardStaysOnTop() {
        harness.addToBattlefield(player1, new ScryingSheets());
        Card topCard = new ThrummingStone();
        harness.setLibrary(player1, List.of(topCard, new SnowCoveredForest()));
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Requires snow mana for the library ability")
    void requiresSnowMana() {
        harness.addToBattlefield(player1, new ScryingSheets());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana from a snow source can pay the snow activation cost")
    void paysWithManaFromSnowSource() {
        Permanent sheets = harness.addToBattlefieldAndReturn(player1, new ScryingSheets());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        Card topCard = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(topCard, new ThrummingStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.tapPermanent(player1, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(sheets.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Looking at a nonsnow card shows its identity only to the controller")
    void privatelyShowsNonSnowTopCard() {
        harness.addToBattlefield(player1, new ScryingSheets());
        harness.setLibrary(player1, List.of(new ThrummingStone()));
        addAbilityMana();
        harness.clearMessages();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("Thrumming Stone")).isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("Thrumming Stone")).isEmpty();
    }

    @Test
    @DisplayName("An empty library offers no reveal and does not lose the game")
    void emptyLibraryDoesNothing() {
        Permanent sheets = harness.addToBattlefieldAndReturn(player1, new ScryingSheets());
        harness.setLibrary(player1, List.of());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(sheets.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The ability looks at the library when it resolves, even after its source leaves")
    void resolvesAfterSourceLeavesUsingCurrentTopCard() {
        harness.addToBattlefield(player1, new ScryingSheets());
        harness.setLibrary(player1, List.of(new ThrummingStone()));
        addAbilityMana();
        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        Card topCard = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(topCard));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }
}
