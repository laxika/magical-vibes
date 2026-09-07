package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VentureDeeper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkSecretkeeper.class, VentureDeeper.class, GrizzlyBears.class})
class MerfolkSecretkeeperTest extends BaseCardTest {

    @Test
    void adventureMillsFourCardsAndExilesTheCard() {
        List<Card> milled = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, milled);
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        MerfolkSecretkeeper card = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merfolk Secretkeeper");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
