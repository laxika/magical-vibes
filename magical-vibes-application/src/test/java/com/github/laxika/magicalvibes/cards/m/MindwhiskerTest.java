package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindwhisker.class, GrizzlyBears.class, Shock.class})
class MindwhiskerTest extends BaseCardTest {

    @Test
    @DisplayName("Weakens opposing creatures with seven or more cards in its controller's graveyard")
    void weakensOpposingCreaturesAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new Mindwhisker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not weaken opposing creatures below the graveyard threshold")
    void doesNotWeakenBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addToBattlefield(player1, new Mindwhisker());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cards in an opponent's graveyard do not satisfy the threshold")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addToBattlefield(player1, new Mindwhisker());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Surveils 1 at the beginning of its controller's upkeep")
    void upkeepSurveilsOne() {
        harness.addToBattlefield(player1, new Mindwhisker());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
