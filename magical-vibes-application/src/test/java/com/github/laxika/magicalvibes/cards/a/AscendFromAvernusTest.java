package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AscendFromAvernus.class, GrizzlyBears.class, GarrukWildspeaker.class,
        ChandraNalaar.class, Plains.class})
class AscendFromAvernusTest extends BaseCardTest {

    @Test
    @DisplayName("Returns own creatures and planeswalkers with mana value X or less and exiles itself")
    void returnsEligibleCardsAndExilesSpell() {
        Card bears = new GrizzlyBears();
        Card garruk = new GarrukWildspeaker();
        Card chandra = new ChandraNalaar();
        Card plains = new Plains();
        Card opponentBears = new GrizzlyBears();
        AscendFromAvernus ascend = new AscendFromAvernus();
        harness.setGraveyard(player1, List.of(bears, garruk, chandra, plains));
        harness.setGraveyard(player2, List.of(opponentBears));
        harness.setHand(player1, List.of(ascend));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(bears, garruk);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(chandra, plains);
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(ascend.getId()));
    }
}
