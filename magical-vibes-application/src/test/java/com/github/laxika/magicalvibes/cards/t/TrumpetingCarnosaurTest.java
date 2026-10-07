package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.OrazcaPuzzleDoor;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrumpetingCarnosaur.class, ElspethKnightErrant.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, OrazcaPuzzleDoor.class, QuintoriusKand.class})
class TrumpetingCarnosaurTest extends BaseCardTest {

    @Test
    @DisplayName("When Trumpeting Carnosaur enters, it discovers 5")
    void discoversFiveWhenItEnters() {
        Forest land = new Forest();
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(land, discovered));
        harness.castFromHand(player1, new TrumpetingCarnosaur(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("The hand ability deals 3 damage to a creature and discards Trumpeting Carnosaur")
    void handAbilityDamagesCreature() {
        harness.setHand(player1, List.of(new TrumpetingCarnosaur()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addHandAbilityMana();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateHandAbility(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Trumpeting Carnosaur");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The hand ability deals 3 damage to a planeswalker")
    void handAbilityDamagesPlaneswalker() {
        harness.setHand(player1, List.of(new TrumpetingCarnosaur()));
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        addHandAbilityMana();

        harness.activateHandAbility(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Trumpeting Carnosaur");
    }

    @Test
    void discoverSkipsLandsAndExpensiveCardsAndStopsAtFirstHit() {
        Forest land = new Forest();
        TrumpetingCarnosaur expensive = new TrumpetingCarnosaur();
        OrazcaPuzzleDoor discovered = new OrazcaPuzzleDoor();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(land, expensive, discovered, remaining));

        harness.castFromHand(player1, new TrumpetingCarnosaur(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(land, expensive, discovered);

        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Orazca Puzzle-Door");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(remaining);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(remaining, land, expensive);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void discoveredSpellIsCastForFreeFromExileAndTriggersQuintorius() {
        harness.addToBattlefield(player1, new QuintoriusKand());
        harness.setLibrary(player1, List.of(new OrazcaPuzzleDoor()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new TrumpetingCarnosaur(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Orazca Puzzle-Door");
        harness.assertNotInHand(player1, "Orazca Puzzle-Door");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void discoverIncludesCardsWithManaValueExactlyFive() {
        QuintoriusKand discovered = new QuintoriusKand();
        harness.setLibrary(player1, List.of(discovered));

        harness.castFromHand(player1, new TrumpetingCarnosaur(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Quintorius Kand");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void discoverCanCastTheFoundCardWithoutManaOfItsColor() {
        harness.setLibrary(player1, List.of(new OrazcaPuzzleDoor()));

        harness.castFromHand(player1, new TrumpetingCarnosaur(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Orazca Puzzle-Door");
        harness.assertNotInHand(player1, "Orazca Puzzle-Door");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void discoverWithNoQualifyingCardReturnsAllCardsToLibrary() {
        Forest land = new Forest();
        TrumpetingCarnosaur expensive = new TrumpetingCarnosaur();
        harness.setLibrary(player1, List.of(land, expensive));

        harness.castFromHand(player1, new TrumpetingCarnosaur(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Trumpeting Carnosaur");
    }

    @Test
    void discoverWithEmptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new TrumpetingCarnosaur(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Trumpeting Carnosaur");
    }

    @Test
    void handAbilityCannotTargetAPlayerOrNoncreatureArtifact() {
        harness.setHand(player1, List.of(new TrumpetingCarnosaur()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        addHandAbilityMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Trumpeting Carnosaur");
        harness.assertNotInGraveyard(player1, "Trumpeting Carnosaur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void handAbilityRequiresRedManaAndDoesNotDiscardWhenPaymentFails() {
        harness.setHand(player1, List.of(new TrumpetingCarnosaur()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrumpetingCarnosaur());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Trumpeting Carnosaur");
        harness.assertNotInGraveyard(player1, "Trumpeting Carnosaur");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void handAbilityDiscardsAsCostAndCanDamageYourOwnCreature() {
        harness.setHand(player1, List.of(new TrumpetingCarnosaur()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TrumpetingCarnosaur());
        addHandAbilityMana();

        harness.activateHandAbility(player1, 0, target.getId());

        harness.assertNotInHand(player1, "Trumpeting Carnosaur");
        harness.assertInGraveyard(player1, "Trumpeting Carnosaur");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Trumpeting Carnosaur");
    }

    private void addHandAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
