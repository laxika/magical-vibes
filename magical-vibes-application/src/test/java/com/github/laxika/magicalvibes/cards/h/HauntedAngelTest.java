package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntedAngel.class, WrathOfGod.class})
class HauntedAngelTest extends BaseCardTest {

    @Test
    @DisplayName("When Haunted Angel dies, it is exiled and each opponent creates a 3/3 black Angel with flying")
    void diesExilesItAndGivesOpponentAnAngel() {
        harness.addToBattlefield(player1, new HauntedAngel());
        Permanent hauntedAngel = findPermanent(player1, "Haunted Angel");

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Haunted Angel");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(hauntedAngel.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(hauntedAngel.getCard().getId()));

        List<Permanent> angels = findPermanents(player2, "Angel");
        assertThat(angels).hasSize(1);
        Permanent angel = angels.getFirst();
        assertThat(angel.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(angel.getCard().getPower()).isEqualTo(3);
        assertThat(angel.getCard().getToughness()).isEqualTo(3);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(angel.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(angel.getCard().isToken()).isTrue();
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }
}
