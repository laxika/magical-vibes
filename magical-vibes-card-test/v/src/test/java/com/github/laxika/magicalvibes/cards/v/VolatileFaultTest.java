package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolatileFault.class, Forest.class, Plains.class, ArmoredKincaller.class})
class VolatileFaultTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new VolatileFault());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an own nonbasic land")
    void cannotTargetOwnNonbasicLand() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new VolatileFault()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys an opponent's nonbasic land, searches for a basic land, and creates a Treasure")
    void destroysLandSearchesAndCreatesTreasure() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new VolatileFault()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player2, List.of(new Forest(), new Plains()));

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Volatile Fault");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC));

        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(findPermanent(player2, "Forest").isTapped()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Treasure when the opponent fails to find a basic land")
    void createsTreasureWhenOpponentFailsToFind() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new VolatileFault()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player2, List.of(new ArmoredKincaller()));

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Volatile Fault");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void opponentCanDeclineSearchWithoutShuffling() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new VolatileFault()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        var forest = new Forest();
        var plains = new Plains();
        harness.setLibrary(player2, List.of(forest, plains));

        harness.activateAbility(player1, 0, 1, null, targetId);

        harness.assertInGraveyard(player1, "Volatile Fault");
        harness.assertOnBattlefield(player2, "Volatile Fault");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Volatile Fault");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest, plains);
        assertThat(gameLogContains("Library is shuffled")).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Volatile Fault");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCanFailToFindEvenWithBasicLandAvailable() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new VolatileFault()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        var forest = new Forest();
        harness.setLibrary(player2, List.of(forest));

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new VolatileFault()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Volatile Fault");
        assertThat(findPermanent(player1, "Volatile Fault").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new VolatileFault()).getId();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Volatile Fault");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void illegalTargetDoesNotCreateTreasureOrOfferSearch() {
        harness.addToBattlefield(player1, new VolatileFault());
        UUID otherLandId = harness.addToBattlefieldAndReturn(player1, new VolatileFault()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new VolatileFault()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.activateAbility(player2, 0, 1, null, otherLandId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
