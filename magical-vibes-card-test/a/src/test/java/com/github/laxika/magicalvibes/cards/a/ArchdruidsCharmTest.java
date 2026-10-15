package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CaseOfTheLockedHothouse;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ArchdruidsCharm.class, Forest.class, GrizzlyBears.class, FountainOfYouth.class,
        CaseOfTheLockedHothouse.class})
class ArchdruidsCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a searched land onto the battlefield tapped")
    void searchesLandToBattlefieldTapped() {
        Forest forest = new Forest();
        castWithLibrary(forest);

        chooseCard(forest);

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a searched creature into hand")
    void searchesCreatureToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        castWithLibrary(bears);

        chooseCard(bears);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().equals(bears));
    }

    @Test
    @DisplayName("Puts a counter on a creature and uses its new power to deal one-way damage")
    void counterThenFightUsesUpdatedPower() {
        GrizzlyBears ownBears = new GrizzlyBears();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.addToBattlefield(player1, ownBears);
        harness.addToBattlefield(player2, opposingBears);
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        harness.castModalInstant(player1, 0, 1, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles a target artifact")
    void exilesArtifact() {
        FountainOfYouth fountain = new FountainOfYouth();
        harness.addToBattlefield(player2, fountain);
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        harness.castModalInstant(player1, 0, 2,
                List.of(harness.getPermanentId(player2, "Fountain of Youth")));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(fountain);
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void exilesOwnEnchantment() {
        CaseOfTheLockedHothouse enchantment = new CaseOfTheLockedHothouse();
        harness.addToBattlefield(player1, enchantment);
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        harness.castModalInstant(player1, 0, 2,
                List.of(harness.getPermanentId(player1, "Case of the Locked Hothouse")));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(enchantment);
        harness.assertNotOnBattlefield(player1, "Case of the Locked Hothouse");
    }

    @Test
    void canDeclineToFindAnAvailableLand() {
        Forest forest = new Forest();
        castWithLibrary(forest);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchDoesNotOfferArtifacts() {
        Forest forest = new Forest();
        FountainOfYouth fountain = new FountainOfYouth();
        harness.setLibrary(player1, List.of(forest, fountain));
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        chooseCard(forest);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fountain);
    }

    @Test
    void stillPlacesCounterWhenOpponentTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent own = findPermanent(player1, "Grizzly Bears");
        Permanent opponent = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();
        harness.castModalInstant(player1, 0, 1, List.of(own.getId(), opponent.getId()));

        harness.getPermanentRemovalService().removePermanentToHand(gd, opponent);
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(own.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void dealsNoDamageWhenOwnTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent own = findPermanent(player1, "Grizzly Bears");
        Permanent opponent = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();
        harness.castModalInstant(player1, 0, 1, List.of(own.getId(), opponent.getId()));

        harness.getPermanentRemovalService().removePermanentToHand(gd, own);
        harness.passBothPriorities();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void damageModeDoesNotDealDamageBackToOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent own = findPermanent(player1, "Grizzly Bears");
        Permanent opponent = findPermanent(player2, "Grizzly Bears");
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        harness.castModalInstant(player1, 0, 1, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(own.getMarkedDamage()).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponent.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotExileOrdinaryCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2,
                List.of(harness.getPermanentId(player2, "Grizzly Bears"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReverseDamageModeControllers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player1, "Grizzly Bears"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void searchResolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Archdruid's Charm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addGreenMana() {
        harness.addMana(player1, ManaColor.GREEN, 3);
    }

    private void castWithLibrary(Card card) {
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    private void chooseCard(Card card) {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int index = search.params().cards().indexOf(card);
        harness.handleCardChosen(player1, index);
    }
}
