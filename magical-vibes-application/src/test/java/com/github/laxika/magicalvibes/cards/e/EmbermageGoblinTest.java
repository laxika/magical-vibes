package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbermageGoblin.class, Mountain.class})
class EmbermageGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may search prompt")
    void enteringTheBattlefieldCreatesMaySearchPrompt() {
        setupAndCast();

        resolveCreatureAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability searches for another copy")
    void acceptingMaySearchesForAnotherCopy() {
        setupAndCast();
        EmbermageGoblin copy = new EmbermageGoblin();
        EmbermageGoblin filler = new EmbermageGoblin();
        harness.setLibrary(player1, List.of(copy, filler));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(copy, filler);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(copy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningMayDoesNotSearch() {
        setupAndCast();
        EmbermageGoblin copy = new EmbermageGoblin();
        harness.setLibrary(player1, List.of(copy));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(copy);
    }

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyGoblin(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void deals1DamageToCreature() {
        addReadyGoblin(player1);
        Permanent target = addReadyGoblin(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Embermage Goblin");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new EmbermageGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent goblin = addReadyGoblin(player1);
        goblin.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Search only offers cards named Embermage Goblin and reveals the chosen card")
    void searchFiltersByNameAndRevealsChosenCard() {
        setupAndCast();
        Mountain other = new Mountain();
        EmbermageGoblin copy = new EmbermageGoblin();
        harness.setLibrary(player1, List.of(other, copy));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(copy);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(copy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gameLogContains("reveals Embermage Goblin")).isTrue();
    }

    @Test
    @DisplayName("A restricted search may fail to find even when a matching copy exists")
    void mayFailToFindMatchingCopy() {
        setupAndCast();
        EmbermageGoblin copy = new EmbermageGoblin();
        harness.setLibrary(player1, List.of(copy));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(copy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting a search with no matching card completes without taking a card")
    void searchWithNoMatchingCardCompletes() {
        setupAndCast();
        Mountain other = new Mountain();
        harness.setLibrary(player1, List.of(other));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting a search of an empty library completes")
    void searchEmptyLibraryCompletes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The Goblin can target itself and tapping is paid before resolution")
    void canTargetItself() {
        Permanent goblin = addReadyGoblin(player1);

        harness.activateAbility(player1, 0, null, goblin.getId());

        assertThat(goblin.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Embermage Goblin");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Embermage Goblin");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(goblin.getCard());
    }

    @Test
    @DisplayName("Activated damage resolves after the Goblin is killed in response")
    void damageResolvesAfterSourceDies() {
        harness.setLife(player2, 20);
        Permanent source = addReadyGoblin(player1);
        addReadyGoblin(player2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player2, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Embermage Goblin");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Any target includes the Goblin's controller")
    void canDamageItsController() {
        harness.setLife(player1, 20);
        addReadyGoblin(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An ordinary land is not a legal damage target")
    void cannotTargetOrdinaryLand() {
        addReadyGoblin(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new EmbermageGoblin()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveCreatureAndTrigger() {
        resolveAllTriggers();
    }

    private Permanent addReadyGoblin(Player player) {
        return addCreatureReady(player, new EmbermageGoblin());
    }
}
