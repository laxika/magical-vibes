package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NiambiBelovedProtector.class, GrizzlyBears.class,
        GiantGrowth.class, Unsummon.class, GrafdiggersCage.class})
class NiambiBelovedProtectorTest extends BaseCardTest {

    @Test
    void returnsOnlyANonlegendaryCreaturePutIntoTheGraveyardFromTheBattlefieldThisTurn() {
        Permanent grizzly = addCreatureReady(player1, new GrizzlyBears());
        Permanent legendary = addCreatureReady(player1, new NiambiBelovedProtector());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, grizzly);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, legendary);
        });

        castNiambi();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(grizzly.getCard().getId());
        assertThat(choice.validCardIds()).doesNotContain(legendary.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(grizzly.getCard().getId()));
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    void targetAbilityDrawsOnlyOnceEachTurn() {
        Permanent grizzly = returnGrizzly();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, grizzly.getId());
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.castInstant(player1, 0, grizzly.getId());
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void targetAbilityIsPerpetualAcrossZoneChanges() {
        Permanent grizzly = returnGrizzly();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, grizzly.getId());
        resolveAllTriggers();

        Card returnedCard = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card == grizzly.getCard())
                .findFirst().orElseThrow();
        harness.castFromHand(player1, returnedCard, "{1}{G}");
        resolveAllTriggers();

        Permanent reenteredGrizzly = findPermanent(player1, "Grizzly Bears");
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, reenteredGrizzly.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDoesNotReturnACreature() {
        Permanent grizzly = addCreatureReady(player1, new GrizzlyBears());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, grizzly));

        harness.enterBattlefieldAndReturn(player1, new NiambiBelovedProtector());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void excludesCardsNotPutThereFromTheBattlefieldAndOpponentsCards() {
        GrizzlyBears oldCard = new GrizzlyBears();
        GiantGrowth noncreature = new GiantGrowth();
        harness.setGraveyard(player1, List.of(oldCard, noncreature));
        Permanent ownGrizzly = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentsGrizzly = addCreatureReady(player2, new GrizzlyBears());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ownGrizzly);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opponentsGrizzly);
        });

        castNiambi();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownGrizzly.getCard().getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownGrizzly.getCard().getId()));
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldCard, noncreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void opponentsTargetingSpellDrawsForTheCreatureController() {
        Permanent grizzly = returnGrizzly();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, grizzly.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void perpetualAbilityIsGrantedEvenWhenTheReturnIsPrevented() {
        Permanent grizzly = addCreatureReady(player1, new GrizzlyBears());
        Permanent cage = harness.addToBattlefieldAndReturn(player1, new GrafdiggersCage());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, grizzly));
        castNiambi();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(grizzly.getCard().getId());
        harness.handleMultipleCardsChosen(player1, List.of(grizzly.getCard().getId()));
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, cage);
            harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, grizzly.getCard().getId());
        });
        harness.castFromHand(player1, grizzly.getCard(), "{1}{G}");
        resolveAllTriggers();
        Permanent reenteredGrizzly = findPermanent(player1, "Grizzly Bears");
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, reenteredGrizzly.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castNiambi() {
        harness.castFromHand(player1, new NiambiBelovedProtector(), "{W}{U}");
        harness.passBothPriorities();
    }

    private Permanent returnGrizzly() {
        Permanent grizzly = addCreatureReady(player1, new GrizzlyBears());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, grizzly));
        castNiambi();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().get(0)));
        resolveAllTriggers();
        return findPermanent(player1, "Grizzly Bears");
    }
}
