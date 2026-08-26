package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSpotLivingPortal.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class})
class TheSpotLivingPortalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles one nonland permanent and one nonland permanent card, then death returns them")
    void etbExilesTargetsAndDeathReturnsThemToOwnersHands() {
        Permanent battlefieldTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setGraveyard(player2, List.of(new HillGiant()));
        TheSpotLivingPortal spot = castSpot();
        UUID graveyardTargetId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();

        harness.handleMultipleCardsChosen(player1,
                List.of(battlefieldTarget.getCard().getId(), graveyardTargetId));
        harness.passBothPriorities();

        Permanent spotPermanent = findPermanentByCardId(spot.getId());
        assertThat(gd.getCardsExiledByPermanent(spotPermanent.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(battlefieldTarget.getCard().getId(), graveyardTargetId);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(battlefieldTarget.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(graveyardTargetId));

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spotPermanent);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).last()
                .extracting(Card::getId)
                .isEqualTo(spot.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spot.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .contains(battlefieldTarget.getCard().getId(), graveyardTargetId);
        assertThat(gd.getCardsExiledByPermanent(spotPermanent.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ETB allows choosing no targets")
    void etbAllowsNoTargets() {
        TheSpotLivingPortal spot = castSpot();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanentByCardId(spot.getId())).isNotNull();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("The ETB allows at most one target from each zone")
    void etbAllowsAtMostOneTargetFromEachZone() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSpot();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstTarget.getCard().getId(), secondTarget.getCard().getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("battlefield");
    }

    private TheSpotLivingPortal castSpot() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        TheSpotLivingPortal spot = new TheSpotLivingPortal();
        harness.setHand(player1, List.of(spot));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return spot;
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
