package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageOrder.class, ColossalDreadmaw.class, FrenziedRaptor.class, GrizzlyBears.class, Confiscate.class})
class SavageOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature with power 4 or greater and puts a Dinosaur onto the battlefield")
    void sacrificesLargeCreatureAndFindsDinosaur() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Card dinosaur = new FrenziedRaptor();
        harness.setLibrary(player1, List.of(dinosaur));
        castSavageOrder(sacrifice);

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(dinosaur);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
        Permanent found = findPermanent(player1, "Frenzied Raptor");
        assertThat(found).isNotNull();
        assertThat(found.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The fetched Dinosaur keeps indestructible through cleanup and loses it on the next turn")
    void indestructibleLastsUntilNextTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new FrenziedRaptor()));
        castSavageOrder(sacrifice);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent found = findPermanent(player1, "Frenzied Raptor");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(found.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(found.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(found.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot cast without a creature with power 4 or greater to sacrifice")
    void requiresLargeCreatureSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageOrder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    void acceptsExactlyFourPowerAndFiltersSearchToDinosaurCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        Card dinosaur = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new SavageOrder(), dinosaur));
        castSavageOrder(sacrifice);

        harness.assertInGraveyard(player1, "Frenzied Raptor");
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(dinosaur);
        harness.handleCardChosen(player1, 0);

        Permanent found = findPermanent(player1, "Colossal Dreadmaw");
        assertThat(found).isNotNull();
        assertThat(found.isTapped()).isFalse();
        assertThat(found.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void mayFailToFindEvenWithAnEligibleDinosaur() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Card dinosaur = new FrenziedRaptor();
        harness.setLibrary(player1, List.of(dinosaur));
        castSavageOrder(sacrifice);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Frenzied Raptor");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dinosaur);
        harness.assertInGraveyard(player1, "Savage Order");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWhenLibraryContainsNoDinosaur() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        castSavageOrder(sacrifice);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
        harness.assertInGraveyard(player1, "Savage Order");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        assertThatThrownBy(() -> castSavageOrder(opponentCreature))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertInHand(player1, "Savage Order");
    }

    @Test
    void indestructibleExpiresOnCastersNextTurnAfterControlChanges() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new FrenziedRaptor(), new GrizzlyBears()));
        castSavageOrder(sacrifice);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        Permanent found = findPermanent(player1, "Frenzied Raptor");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player2, 0, found.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Frenzied Raptor");
        assertThat(gqs.hasKeyword(gd, found, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, found, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castSavageOrder(Permanent sacrifice) {
        harness.setHand(player1, List.of(new SavageOrder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
    }

}
