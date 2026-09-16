package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExcavatingAnurid.class, Forest.class, Shock.class})
class ExcavatingAnuridTest extends BaseCardTest {

    @Test
    @DisplayName("May sacrifice a land when it enters to draw a card")
    void maySacrificeLandToDraw() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card drawnCard = new Shock();
        harness.setLibrary(player1, List.of(drawnCard));
        castAnurid(player1);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Declining the ETB ability does not sacrifice a land or draw")
    void decliningDoesNotSacrificeOrDraw() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Shock()));
        castAnurid(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Threshold gives it +1/+1 and vigilance")
    void thresholdBoostsAndGrantsVigilance() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent anurid = harness.addToBattlefieldAndReturn(player1, new ExcavatingAnurid());

        assertThat(gqs.getEffectivePower(gd, anurid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, anurid)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, anurid, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Threshold does not apply below seven cards in its controller's graveyard")
    void noThresholdBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        Permanent anurid = harness.addToBattlefieldAndReturn(player1, new ExcavatingAnurid());

        assertThat(gqs.getEffectivePower(gd, anurid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anurid)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, anurid, Keyword.VIGILANCE)).isFalse();
    }

    private void castAnurid(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, new ArrayList<>(List.of(new ExcavatingAnurid())));
        harness.addMana(player, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);

        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
    }

    private List<Card> graveyardWithSevenCards() {
        return graveyardWithCards(7);
    }

    private List<Card> graveyardWithCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
