package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryFall.class, AvatarOfMight.class, GrizzlyBears.class, HillGiant.class,
        Plains.class, Forest.class, Island.class})
class FieryFallTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to target creature, killing one with 5 or less toughness")
    void deals5DamageKillingSmallCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A creature with more than 5 toughness survives with 5 marked damage")
    void bigCreatureSurvivesWith5MarkedDamage() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        // Avatar of Might is 8/8 — survives 5 damage.
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        harness.castInstant(player1, 0, avatar.getId());
        harness.passBothPriorities();

        Permanent survivor = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(avatar.getId()))
                .findFirst().orElseThrow();
        assertThat(survivor.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("The spell goes to its owner's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fiery Fall");
    }

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingDiscardsAndSearches() {
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fiery Fall");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);
    }

    @Test
    @DisplayName("Choosing a basic land from the search puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, chosenName);
    }

    @Test
    @DisplayName("Basic landcycling discards as a cost before the search resolves")
    void discardsBeforeSearchResolves() {
        harness.setHand(player1, List.of(new FieryFall()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Fiery Fall");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling may fail to find even with a basic land available")
    void mayFailToFind() {
        harness.setHand(player1, List.of(new FieryFall()));
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fiery Fall");
    }

    @Test
    @DisplayName("Basic landcycling resolves without drawing when the library has no basic lands")
    void noBasicLandsDoesNotDraw() {
        harness.setHand(player1, List.of(new FieryFall()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling requires red mana and does not discard on an illegal activation")
    void cannotCycleWithoutRedMana() {
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Fiery Fall");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fiery Fall cannot target a player")
    void cannotTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Fiery Fall");
        assertThat(gd.stack).isEmpty();
    }
    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
