package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LakeTownLookout.class, Shock.class, Forest.class, GrizzlyBears.class})
class LakeTownLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, recruit creates a Soldier after discarding a nonland card")
    void diesAndRecruitsAfterNonlandDiscard() {
        Permanent lookout = prepareLookout(new GrizzlyBears(), new Forest());

        killLookout(lookout);

        harness.assertInGraveyard(player1, "Lake-town Lookout");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("When it dies, recruit does not create a Soldier after discarding a land")
    void diesAndRecruitsAfterLandDiscard() {
        Permanent lookout = prepareLookout(new Forest(), new GrizzlyBears());

        killLookout(lookout);

        harness.assertInGraveyard(player1, "Lake-town Lookout");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Recruit creates a 1/1 white Human Soldier token")
    void recruitTokenHasBothCreatureTypes() {
        Permanent lookout = prepareLookout(new GrizzlyBears(), new Forest());
        killLookout(lookout);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Recruit creates its token during resolution without another priority round")
    void recruitCreatesTokenDuringSameResolution() {
        Permanent lookout = prepareLookout(new GrizzlyBears(), new Forest());
        killLookout(lookout);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Recruit with an empty hand discards the card it just drew")
    void recruitDiscardsDrawnCardFromEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
        killLookout(lookout);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent prepareLookout(Card discardedCard, Card drawnCard) {
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        return harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
    }

    private void killLookout(Permanent lookout) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, lookout.getId());
        harness.passBothPriorities();
    }
}
