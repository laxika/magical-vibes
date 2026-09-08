package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
