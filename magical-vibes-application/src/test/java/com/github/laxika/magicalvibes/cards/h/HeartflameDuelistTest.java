package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartflameDuelist.class, HeartflameSlash.class, GrizzlyBears.class, Shock.class})
class HeartflameDuelistTest extends BaseCardTest {

    @Test
    void adventureDealsThreeDamageAndExilesTheCard() {
        HeartflameDuelist card = new HeartflameDuelist();
        harness.setHand(player1, List.of(card));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        HeartflameDuelist card = new HeartflameDuelist();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heartflame Duelist");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void controllerInstantGainsLifelink() {
        harness.addToBattlefield(player1, new HeartflameDuelist());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
