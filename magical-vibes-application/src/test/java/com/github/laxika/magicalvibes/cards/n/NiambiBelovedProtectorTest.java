package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NiambiBelovedProtector.class, NiambiEsteemedSpeaker.class, GrizzlyBears.class,
        GiantGrowth.class, Unsummon.class})
class NiambiBelovedProtectorTest extends BaseCardTest {

    @Test
    void returnsOnlyANonlegendaryCreaturePutIntoTheGraveyardFromTheBattlefieldThisTurn() {
        Permanent grizzly = addCreatureReady(player1, new GrizzlyBears());
        Permanent legendary = addCreatureReady(player1, new NiambiEsteemedSpeaker());
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
        harness.setHand(player1, List.of(returnedCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new NiambiBelovedProtector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
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
