package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BristlebudFarmer.class, Forest.class, GrizzlyBears.class, Opt.class})
class BristlebudFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two Food tokens")
    void createsTwoFoodTokensOnEntry() {
        castAndResolve();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking may sacrifice a Food to mill three cards and return a milled permanent")
    void sacrificesFoodToMillAndReturnPermanent() {
        castAndResolve();
        harness.setLibrary(player1, List.of(new Forest(), new Opt(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Food"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the attack trigger keeps the Food and library unchanged")
    void declinesFoodSacrifice() {
        castAndResolve();
        harness.setLibrary(player1, List.of(new Forest(), new Opt(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling happens during the attack ability, without another priority round")
    void millsBeforePlayersCanRespondAfterSacrifice() {
        castAndResolve();
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new BristlebudFarmer(), new BristlebudFarmer(), new BristlebudFarmer()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Food"));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("All milled permanents may be declined without undoing the sacrifice or mill")
    void declinesEveryMilledPermanent() {
        castAndResolve();
        harness.setLibrary(player1, List.of(new BristlebudFarmer(), new BristlebudFarmer(), new BristlebudFarmer()));

        sacrificeFoodAndResolveMill();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A later milled permanent can be chosen, and only one card is returned")
    void choosesLaterPermanentAndCannotReturnAnother() {
        castAndResolve();
        harness.setLibrary(player1, List.of(new Forest(), new BristlebudFarmer(), new Forest()));

        sacrificeFoodAndResolveMill();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Bristlebud Farmer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A short library mills only its remaining cards and does not return an older graveyard card")
    void shortLibraryWithoutMilledPermanent() {
        castAndResolve();
        harness.setGraveyard(player1, List.of(new BristlebudFarmer()));
        harness.setLibrary(player1, List.of(new Opt()));

        sacrificeFoodAndResolveMill();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player1, "Bristlebud Farmer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Attacking without a Food does not mill")
    void noFoodDoesNotMill() {
        addCreatureReady(player1, new BristlebudFarmer());
        harness.setLibrary(player1, List.of(new BristlebudFarmer()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Created Food can be tapped and sacrificed for two mana to gain three life")
    void activatesCreatedFood() {
        castAndResolve();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertLife(player1, 23);
    }

    private void sacrificeFoodAndResolveMill() {
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Food"));
        resolveAllTriggers();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new BristlebudFarmer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        findPermanent(player1, "Bristlebud Farmer").setSummoningSick(false);
    }
}
