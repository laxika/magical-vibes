package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HERBIEScoutUnit.class, Forest.class, GrizzlyBears.class})
class HERBIEScoutUnitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card, then may put a land from hand onto the battlefield tapped")
    void drawsThenPutsLandTapped() {
        HERBIEScoutUnit scout = new HERBIEScoutUnit();
        Forest land = new Forest();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(scout, land)));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent permanent = findPermanent(land);
        assertThat(permanent).isNotNull();
        assertThat(permanent.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the land placement still draws a card")
    void declineStillDraws() {
        HERBIEScoutUnit scout = new HERBIEScoutUnit();
        Forest land = new Forest();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(scout, land)));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanent(land)).isNull();
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
