package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AureliasVindicator.class, GrizzlyBears.class, HillGiant.class,
        PincherBeetles.class, Shock.class, Unsummon.class})
class AureliasVindicatorTest extends BaseCardTest {

    @Test
    @DisplayName("Disguise turns face up for the chosen X and exiles creatures across both zones")
    void disguiseExilesUpToChosenXCreaturesAcrossBothZones() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new HillGiant()));

        castFaceDown();
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        UUID giantId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Aurelia's Vindicator")));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TurnFaceUpXValueChoice.class);
        harness.handleXValueChosen(player1, 2);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getCard().getId(), giantId));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Aurelia's Vindicator").isFaceDown()).isFalse();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Hill Giant");
    }

    @Test
    @DisplayName("Cards exiled by the face-up ability return to their owners' hands when it leaves")
    void exiledCardsReturnWhenVindicatorLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new HillGiant()));
        castAndResolveFaceUpWithExile();

        UUID vindicatorId = harness.getPermanentId(player1, "Aurelia's Vindicator");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, vindicatorId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Disguise's ward protects the face-down creature")
    void disguiseGrantsWardFaceDown() {
        castFaceDown();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Aurelia's Vindicator"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(findPermanent(player1, "Aurelia's Vindicator").isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("A battlefield target that dies in response is not exiled from its graveyard")
    void dyingTargetIsNotExiledFromItsNewZone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new HillGiant()));
        castFaceDown();
        UUID bearsCardId = findPermanent(player2, "Grizzly Bears").getCard().getId();
        UUID bearsPermanentId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();
        turnFaceUpChoosingX(2);
        harness.handleMultipleCardsChosen(player1, List.of(bearsCardId, giantId));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsPermanentId);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactly("Hill Giant");
    }

    @Test
    @DisplayName("Exiled cards stay in exile until the leaves-the-battlefield trigger resolves")
    void returnWaitsForLeavesBattlefieldTrigger() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new HillGiant()));
        castAndResolveFaceUpWithExile();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Aurelia's Vindicator"));

        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Grizzly Bears", "Hill Giant");
        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Targeting an opposing Vindicator with the exile ability triggers its ward")
    void exileAbilityTriggersOpposingWard() {
        harness.addToBattlefield(player2, new AureliasVindicator());
        castFaceDown();
        UUID targetCardId = findPermanent(player2, "Aurelia's Vindicator").getCard().getId();
        turnFaceUpChoosingX(1);
        harness.handleMultipleCardsChosen(player1, List.of(targetCardId));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Aurelia's Vindicator");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing zero targets leaves available creatures untouched")
    void canChooseZeroTargetsWithPositiveX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFaceDown();
        turnFaceUpChoosingX(1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Aurelia's Vindicator").isFaceDown()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaving before the exile trigger resolves exiles its targets indefinitely")
    void leavingBeforeExileResolutionDoesNotPreventExile() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFaceDown();
        UUID targetCardId = findPermanent(player2, "Grizzly Bears").getCard().getId();
        turnFaceUpChoosingX(1);
        harness.handleMultipleCardsChosen(player1, List.of(targetCardId));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Aurelia's Vindicator"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Disguise can be paid with zero for X")
    void canTurnFaceUpWithZeroX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFaceDown();
        turnFaceUpChoosingX(0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Aurelia's Vindicator").isFaceDown()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Its controller's graveyard cards can be exiled and returned to their hand")
    void returnsOwnGraveyardCardToHand() {
        harness.setGraveyard(player1, List.of(new HillGiant()));
        castFaceDown();
        UUID giantId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        turnFaceUpChoosingX(1);
        harness.handleMultipleCardsChosen(player1, List.of(giantId));
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player1, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).containsExactly("Hill Giant");

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Aurelia's Vindicator"));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Hill Giant");
        harness.assertNotInHand(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Ward protects a face-up Vindicator from an opposing spell")
    void wardProtectsFaceUpVindicator() {
        harness.addToBattlefield(player1, new AureliasVindicator());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Aurelia's Vindicator"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aurelia's Vindicator");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Creatures with shroud are excluded from the exile ability's legal choices")
    void cannotChooseCreatureWithShroud() {
        harness.addToBattlefield(player2, new PincherBeetles());
        harness.setGraveyard(player2, List.of(new HillGiant()));
        castFaceDown();
        turnFaceUpChoosingX(2);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).extracting(Card::getName)
                .contains("Hill Giant").doesNotContain("Pincher Beetles");
    }

    private void turnFaceUpChoosingX(int x) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3 + x);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Aurelia's Vindicator")));
        harness.handleXValueChosen(player1, x);
    }

    private void castFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        AureliasVindicator vindicator = new AureliasVindicator();
        harness.setHand(player1, List.of(vindicator));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Aurelia's Vindicator").isFaceDown()).isTrue();
    }

    private void castAndResolveFaceUpWithExile() {
        castFaceDown();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Aurelia's Vindicator")));
        harness.handleXValueChosen(player1, 2);
        List<UUID> ids = List.of(
                gd.playerBattlefields.get(player2.getId()).getFirst().getCard().getId(),
                gd.playerGraveyards.get(player2.getId()).getFirst().getId());
        harness.handleMultipleCardsChosen(player1, ids);
        harness.passBothPriorities();
    }
}
