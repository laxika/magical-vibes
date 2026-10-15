package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidisiUndeadVizier.class, DragonScarredBear.class, UltimatePrice.class})
class SidisiUndeadVizierTest extends BaseCardTest {

    @Test
    void decliningExploitDoesNotOfferSearch() {
        harness.setLibrary(player1, List.of(new DragonScarredBear()));
        castSidisi();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sidisi, Undead Vizier");
    }

    @Test
    void exploitingCreatureOffersOptionalLibrarySearch() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.setLibrary(player1, List.of(new DragonScarredBear()));
        castSidisi();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
        assertThat(offered).hasSize(1);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Dragon-Scarred Bear");
        harness.assertInGraveyard(player1, "Dragon-Scarred Bear");
    }

    @Test
    void decliningSearchLeavesCardInLibrary() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.setLibrary(player1, List.of(new DragonScarredBear()));
        castSidisi();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Dragon-Scarred Bear");
        harness.assertInGraveyard(player1, "Dragon-Scarred Bear");
    }

    @Test
    void exploitingItselfStillAllowsSearchingForANoncreatureCard() {
        harness.setLibrary(player1, List.of(new UltimatePrice()));
        castSidisi();
        UUID sidisiId = harness.getPermanentId(player1, "Sidisi, Undead Vizier");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sidisiId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Sidisi, Undead Vizier");
        harness.assertInGraveyard(player1, "Sidisi, Undead Vizier");
        harness.assertInHand(player1, "Ultimate Price");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void acceptingSearchWithAnEmptyLibraryCompletesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        castSidisi();
        UUID sidisiId = harness.getPermanentId(player1, "Sidisi, Undead Vizier");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sidisiId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sidisi, Undead Vizier");
    }

    @Test
    void sidisiLeavingBeforeExploitResolvesDoesNotOfferSearch() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.setLibrary(player1, List.of(new DragonScarredBear()));
        castSidisiSpell();
        harness.passBothPriorities();
        UUID sidisiId = harness.getPermanentId(player1, "Sidisi, Undead Vizier");
        harness.setHand(player2, List.of(new UltimatePrice()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, sidisiId);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sidisi, Undead Vizier");
        harness.assertInGraveyard(player1, "Dragon-Scarred Bear");
        harness.assertNotInHand(player1, "Dragon-Scarred Bear");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void searchAbilityStillResolvesAfterSidisiIsDestroyed() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.setLibrary(player1, List.of(new UltimatePrice()));
        castSidisi();
        UUID sidisiId = harness.getPermanentId(player1, "Sidisi, Undead Vizier");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.setHand(player2, List.of(new UltimatePrice()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, sidisiId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Ultimate Price");
        harness.assertInGraveyard(player1, "Sidisi, Undead Vizier");
        harness.assertInGraveyard(player1, "Dragon-Scarred Bear");
    }

    private void castSidisi() {
        castSidisiSpell();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castSidisiSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SidisiUndeadVizier()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
    }
}
