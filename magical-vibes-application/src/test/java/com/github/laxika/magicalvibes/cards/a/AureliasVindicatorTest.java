package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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
        Shock.class, Unsummon.class})
class AureliasVindicatorTest extends BaseCardTest {

    @Test
    @DisplayName("Disguise turns face up for the chosen X and exiles creatures across both zones")
    void disguiseExilesUpToChosenXCreaturesAcrossBothZones() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new HillGiant()));

        AureliasVindicator vindicator = castFaceDown();
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
        harness.castInstant(player1, 0, vindicatorId);
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

    private AureliasVindicator castFaceDown() {
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
        return vindicator;
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
