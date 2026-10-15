package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MerfolkLooter;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandScout.class, Desert.class, Forest.class, GrizzlyBears.class, ZuranOrb.class,
        Millstone.class, MerfolkLooter.class})
class SandScoutTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for a Desert and puts it onto the battlefield tapped")
    void searchesForTappedDesertWhenOpponentHasMoreLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Desert(), new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new SandScout());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(card -> card.getName().equals("Desert"));

        harness.handleCardChosen(player1, 0);

        Permanent desert = findPermanent(player1, "Desert");
        assertThat(desert.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not search when land counts are equal")
    void doesNotSearchWhenOpponentDoesNotHaveMoreLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Desert()));

        harness.enterBattlefieldAndReturn(player1, new SandScout());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Land graveyard trigger creates one multicolor Sand Warrior per turn")
    void createsOnlyOneSandWarriorPerTurn() {
        harness.addToBattlefield(player1, new SandScout());
        harness.addToBattlefield(player1, new ZuranOrb());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handlePermanentChosen(player1, firstLand.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(1);
        Permanent token = findPermanent(player1, "Sand Warrior");
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN, CardColor.WHITE);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(1);
    }

    @Test
    void doesNotSearchIfLandCountsBecomeEqualBeforeResolution() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Desert()));
        harness.enterBattlefieldAndReturn(player1, new SandScout());
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Forest());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Desert")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void canFailToFindDesertEvenWhenOneIsAvailable() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Desert()));
        harness.enterBattlefieldAndReturn(player1, new SandScout());
        resolveAllTriggers();

        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Desert")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void millingTwoLandsCreatesOneTokenForEachScout() {
        harness.addToBattlefield(player1, new SandScout());
        harness.addToBattlefield(player1, new SandScout());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(2);
        for (Permanent token : findPermanents(player1, "Sand Warrior")) {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        }
    }

    @Test
    void millingNonlandsDoesNotUseTheOncePerTurnTrigger() {
        harness.addToBattlefield(player1, new SandScout());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new SandScout()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, player1.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Sand Warrior")).isEmpty();

        harness.activateAbility(player1, 2, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(1);
    }

    @Test
    void opponentsLandEnteringTheirGraveyardDoesNotTrigger() {
        harness.addToBattlefield(player1, new SandScout());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Sand Warrior")).isEmpty();
        assertThat(findPermanents(player2, "Sand Warrior")).isEmpty();
    }

    @Test
    void canTriggerAgainOnOpponentsTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SandScout());
        harness.addToBattlefield(player1, new ZuranOrb());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handlePermanentChosen(player1, firstLand.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(2);
    }

    @Test
    void discardingLandFromHandCreatesToken() {
        harness.addToBattlefield(player1, new SandScout());
        addCreatureReady(player1, new MerfolkLooter());
        Forest land = new Forest();
        harness.setHand(player1, List.of(land));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(findPermanents(player1, "Sand Warrior")).hasSize(1);
    }
}
