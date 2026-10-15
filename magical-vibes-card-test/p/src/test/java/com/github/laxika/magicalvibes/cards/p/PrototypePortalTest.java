package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.r.Riftsweeper;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrototypePortal.class, GolemsHeart.class, AccordersShield.class, CarapaceForger.class, Shatter.class, Riftsweeper.class})
class PrototypePortalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers may ability to exile artifact from hand")
    void etbTriggersImprintChoice() {
        harness.setHand(player1, List.of(new PrototypePortal(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Portal → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting imprint exiles artifact from hand and imprints it")
    void acceptImprintExilesAndImprints() {
        harness.setHand(player1, List.of(new PrototypePortal(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Portal → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        // Accept the may ability (inner effect resolves inline)
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Should be awaiting card choice from hand
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);

        // Choose the artifact (index 0 in remaining hand)
        harness.handleCardChosen(player1, 0);

        // Golem's Heart should be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Golem's Heart"));

        // Golem's Heart should no longer be in hand
        harness.assertNotInHand(player1, "Golem's Heart");

        // Portal should have Golem's Heart imprinted
        Permanent portal = findPermanent(player1, "Prototype Portal");
        assertThat(gd.getImprintedCard(portal.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(portal.getCard()).getName()).isEqualTo("Golem's Heart");
    }

    @Test
    @DisplayName("Declining imprint leaves artifact in hand")
    void declineImprintLeavesCardInHand() {
        harness.setHand(player1, List.of(new PrototypePortal(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Portal → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Golem's Heart should still be in hand
        harness.assertInHand(player1, "Golem's Heart");

        // Portal should have nothing imprinted
        Permanent portal = findPermanent(player1, "Prototype Portal");
        assertThat(gd.getImprintedCard(portal.getCard())).isNull();
    }

    @Test
    @DisplayName("No artifacts in hand skips imprint gracefully")
    void noArtifactsInHandSkips() {
        harness.setHand(player1, List.of(new PrototypePortal(), new CarapaceForger()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Portal → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        // Accept may — but there are no artifacts in hand (inner effect resolves inline → no artifacts → skip)
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        harness.assertInHand(player1, "Carapace Forger");

        // Portal should have nothing imprinted
        Permanent portal = findPermanent(player1, "Prototype Portal");
        assertThat(gd.getImprintedCard(portal.getCard())).isNull();
    }

    @Test
    @DisplayName("Activated ability creates a token copy of the imprinted artifact")
    void activateCreatesTokenCopy() {
        // Set up Portal with an imprinted artifact via addToBattlefield
        PrototypePortal portalCard = new PrototypePortal();
        GolemsHeart heartCard = new GolemsHeart();
        gd.setImprintedCard(portalCard, heartCard);
        harness.setExile(player1, List.of(heartCard));
        harness.addToBattlefield(player1, portalCard);

        // Golem's Heart has mana value 2, so X=2
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities(); // Resolve activated ability

        GameData gd = harness.getGameData();

        // A token copy of Golem's Heart should be on the battlefield
        Permanent tokenHeart = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem's Heart") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(tokenHeart).isNotNull();
    }

    @Test
    @DisplayName("Token is NOT exiled at end step (unlike Mimic Vat)")
    void tokenIsPermanent() {
        PrototypePortal portalCard = new PrototypePortal();
        AccordersShield shieldCard = new AccordersShield();
        gd.setImprintedCard(portalCard, shieldCard);
        harness.setExile(player1, List.of(shieldCard));
        harness.addToBattlefield(player1, portalCard);

        // Accorder's Shield has mana value 0, so X=0
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities(); // Resolve activated ability

        GameData gd = harness.getGameData();

        // Token should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Accorder's Shield") && p.getCard().isToken());

        // Advance to end step
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        // Token should still be on the battlefield (not exiled)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Accorder's Shield") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot activate ability when nothing is imprinted")
    void cannotActivateWhenNothingImprinted() {
        harness.addToBattlefield(player1, new PrototypePortal());

        // Per ruling: "You may not activate the second ability if no card has been exiled with Prototype Portal."
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No card has been exiled with");
    }

    @Test
    @DisplayName("X must equal mana value of imprinted card")
    void xMustMatchManaValue() {
        PrototypePortal portalCard = new PrototypePortal();
        GolemsHeart heartCard = new GolemsHeart();
        gd.setImprintedCard(portalCard, heartCard);
        harness.setExile(player1, List.of(heartCard));
        harness.addToBattlefield(player1, portalCard);

        // Golem's Heart has mana value 2, so X=3 should fail
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must equal the mana value of the imprinted card");
    }

    @Test
    @DisplayName("Imprint can exile a card even after Portal is destroyed in response to its trigger")
    void imprintResolvesAfterPortalLeaves() {
        harness.setHand(player1, List.of(new PrototypePortal(), new AccordersShield()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Prototype Portal").getId());
        harness.assertInGraveyard(player1, "Prototype Portal");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertNotInHand(player1, "Accorder's Shield");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Accorder's Shield"));
    }

    @Test
    @DisplayName("An activated token ability still resolves after Portal is destroyed")
    void tokenAbilityResolvesAfterPortalLeaves() {
        castPortalAndImprintShield();
        harness.activateAbility(player1, 0, 0, null);

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Prototype Portal").getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prototype Portal");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Accorder's Shield"));
    }

    @Test
    @DisplayName("Cannot activate after Riftsweeper moves the imprinted card out of exile")
    void cannotActivateWhenImprintedCardLeavesExile() {
        castPortalAndImprintShield();
        var shield = gd.getPlayerExiledCards(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Riftsweeper()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(shield.getId()));

        harness.passBothPriorities();
        assertThat(gd.findExiledCard(shield.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(shield);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPortalAndImprintShield() {
        harness.setHand(player1, List.of(new PrototypePortal(), new AccordersShield()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
    }
}
