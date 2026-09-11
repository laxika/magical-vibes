package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TillAndTend;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeornReluctantHost.class, TillAndTend.class, Forest.class})
class BeornReluctantHostTest extends BaseCardTest {

    @Test
    void adventureGrantsAnAdditionalLandPlayAndExilesBeorn() {
        BeornReluctantHost card = new BeornReluctantHost();
        harness.setHand(player1, List.of(card, new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        BeornReluctantHost card = new BeornReluctantHost();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beorn, Reluctant Host");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
