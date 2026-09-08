package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PullFromEternity.class, Shock.class})
class PullFromEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a face-up exiled card into its owner's graveyard")
    void putsFaceUpExiledCardIntoOwnersGraveyard() {
        Shock exiledCard = new Shock();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiledCard);
    }

    @Test
    @DisplayName("Cannot target a face-down exiled card")
    void cannotTargetFaceDownExiledCard() {
        Shock exiledCard = new Shock();
        gd.addToExile(player1.getId(), exiledCard, null, true);
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found in exile");
    }
}
