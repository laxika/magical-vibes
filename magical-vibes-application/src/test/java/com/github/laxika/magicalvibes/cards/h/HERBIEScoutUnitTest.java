package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HERBIEScoutUnit.class, Forest.class})
class HERBIEScoutUnitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card, then may put a land from hand onto the battlefield tapped")
    void drawsThenPutsLandTapped() {
        HERBIEScoutUnit scout = new HERBIEScoutUnit();
        Forest land = new Forest();
        HERBIEScoutUnit drawn = new HERBIEScoutUnit();
        harness.setHand(player1, List.of(scout, land));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInHand(player1, "H.E.R.B.I.E. Scout Unit");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent permanent = findPermanent(player1, "Forest");
        assertThat(permanent).isNotNull();
        assertThat(permanent.isTapped()).isTrue();
        harness.assertInHand(player1, "H.E.R.B.I.E. Scout Unit");
    }

    @Test
    @DisplayName("Declining the land placement still draws a card")
    void declineStillDraws() {
        HERBIEScoutUnit scout = new HERBIEScoutUnit();
        Forest land = new Forest();
        HERBIEScoutUnit drawn = new HERBIEScoutUnit();
        harness.setHand(player1, List.of(scout, land));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "H.E.R.B.I.E. Scout Unit");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The land just drawn can be put onto the battlefield tapped")
    void canPutDrawnLandOntoBattlefield() {
        Forest land = new Forest();
        harness.setHand(player1, List.of(new HERBIEScoutUnit()));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting with no land in hand finishes without placing a nonland")
    void noLandInHandStillFinishes() {
        HERBIEScoutUnit drawn = new HERBIEScoutUnit();
        harness.setHand(player1, List.of(new HERBIEScoutUnit()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(countPermanents(player1, "H.E.R.B.I.E. Scout Unit")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
