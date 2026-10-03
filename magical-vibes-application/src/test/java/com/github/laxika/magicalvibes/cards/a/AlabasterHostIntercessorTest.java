package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EpharasDispersal;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IchorDrinker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Scrollshift;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({AlabasterHostIntercessor.class, IchorDrinker.class, Plains.class, Forest.class, EpharasDispersal.class, Scrollshift.class})
class AlabasterHostIntercessorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player2, new IchorDrinker());
        UUID targetId = harness.getPermanentId(player2, "Ichor Drinker");
        castIntercessor(targetId);

        harness.assertNotOnBattlefield(player2, "Ichor Drinker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Ichor Drinker"));
    }

    @Test
    @DisplayName("Exiled creature returns when Alabaster Host Intercessor leaves the battlefield")
    void exiledCreatureReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new IchorDrinker());
        castIntercessor(harness.getPermanentId(player2, "Ichor Drinker"));

        harness.setHand(player2, List.of(new EpharasDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLibrary(player2, List.of());
        UUID intercessorId = harness.getPermanentId(player1, "Alabaster Host Intercessor");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, intercessorId);

        harness.assertOnBattlefield(player2, "Ichor Drinker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Ichor Drinker"));
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new IchorDrinker());
        UUID targetId = harness.getPermanentId(player1, "Ichor Drinker");
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingSearchesForPlains() {
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new IchorDrinker()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alabaster Host Intercessor");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1).allMatch(card -> card.getName().equals("Plains"));

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("The original ETB does nothing if its source leaves and returns before resolution")
    void originalTriggerDoesNotUseReturnedSource() {
        UUID originalTargetId = harness.addToBattlefieldAndReturn(player2, new IchorDrinker()).getId();
        UUID newTargetId = harness.addToBattlefieldAndReturn(player2, new IchorDrinker()).getId();
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0, 0, originalTargetId);
        harness.passBothPriorities();

        UUID originalSourceId = harness.getPermanentId(player1, "Alabaster Host Intercessor");
        harness.setHand(player1, List.of(new Scrollshift()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, originalSourceId);
        harness.handlePermanentChosen(player1, newTargetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Alabaster Host Intercessor"))
                .isNotEqualTo(originalSourceId);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(originalTargetId))
                .noneMatch(permanent -> permanent.getId().equals(newTargetId));
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The ETB does nothing if the source leaves before resolution")
    void sourceLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new IchorDrinker());
        UUID targetId = harness.getPermanentId(player2, "Ichor Drinker");
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new EpharasDispersal()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Alabaster Host Intercessor"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ichor Drinker");
        harness.assertInHand(player1, "Alabaster Host Intercessor");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Plainscycling may fail to find even when a Plains is available")
    void plainscyclingMayFailToFind() {
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Alabaster Host Intercessor");
        harness.assertNotInHand(player1, "Alabaster Host Intercessor");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Plainscycling resolves without drawing when no Plains exists")
    void plainscyclingWithoutPlains() {
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.setLibrary(player1, List.of(new Forest(), new IchorDrinker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alabaster Host Intercessor");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Plainscycling cannot be activated without paying two mana")
    void plainscyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Alabaster Host Intercessor");
        harness.assertNotInGraveyard(player1, "Alabaster Host Intercessor");
        assertThat(gd.stack).isEmpty();
    }

    private void castIntercessor(UUID targetId) {
        harness.setHand(player1, List.of(new AlabasterHostIntercessor()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
