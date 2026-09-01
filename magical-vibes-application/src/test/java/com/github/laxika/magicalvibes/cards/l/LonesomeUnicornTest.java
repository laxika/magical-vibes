package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.r.RiderInNeed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LonesomeUnicorn.class, RiderInNeed.class})
class LonesomeUnicornTest extends BaseCardTest {

    @Test
    void adventureCreatesVigilantKnightAndExilesTheCard() {
        LonesomeUnicorn card = new LonesomeUnicorn();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.getCard().getPower()).isEqualTo(2);
        assertThat(knight.getCard().getToughness()).isEqualTo(2);
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(knight.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(knight.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        LonesomeUnicorn card = new LonesomeUnicorn();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lonesome Unicorn");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNull();
    }
}
