package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WitherbloomCampus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarnessInfinity.class, FieryTemper.class, LightningBolt.class, Shock.class,
        Forest.class, WitherbloomCampus.class})
class HarnessInfinityTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges the hand and graveyard and exiles itself")
    void exchangesHandAndGraveyard() {
        HarnessInfinity harnessInfinity = new HarnessInfinity();
        FieryTemper handCard = new FieryTemper();
        LightningBolt otherHandCard = new LightningBolt();
        Shock graveyardCard = new Shock();
        Forest otherGraveyardCard = new Forest();
        harness.setHand(player1, List.of(harnessInfinity, handCard, otherHandCard));
        harness.setGraveyard(player1, List.of(graveyardCard, otherGraveyardCard));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCard, otherGraveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(handCard, otherHandCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(harnessInfinity);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exchanges when one of the zones is empty")
    void exchangesWithEmptyGraveyard() {
        HarnessInfinity harnessInfinity = new HarnessInfinity();
        LightningBolt handCard = new LightningBolt();
        harness.setHand(player1, List.of(harnessInfinity, handCard));
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(harnessInfinity);
    }

    @Test
    @DisplayName("Returns the graveyard to an empty hand without affecting the opponent")
    void exchangesWithEmptyHand() {
        HarnessInfinity harnessInfinity = new HarnessInfinity();
        WitherbloomCampus graveyardCard = new WitherbloomCampus();
        WitherbloomCampus opponentHandCard = new WitherbloomCampus();
        WitherbloomCampus opponentGraveyardCard = new WitherbloomCampus();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));

        harness.castFromHand(player1, harnessInfinity, "{1}{B}{B}{B}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(harnessInfinity);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
    }

    @Test
    @DisplayName("Exiles itself even when both exchanged zones are empty")
    void exilesItselfWithBothZonesEmpty() {
        HarnessInfinity harnessInfinity = new HarnessInfinity();
        harness.setGraveyard(player1, List.of());

        harness.castFromHand(player1, harnessInfinity, "{1}{B}{B}{B}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(harnessInfinity);
    }
}
