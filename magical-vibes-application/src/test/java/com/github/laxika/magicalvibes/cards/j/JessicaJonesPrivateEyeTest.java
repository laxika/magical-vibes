package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JessicaJonesPrivateEye.class, Forest.class})
class JessicaJonesPrivateEyeTest extends BaseCardTest {

    @Test
    void exilesCardsEqualToPowerAndGrantsPlayPermission() {
        Forest topCard = new Forest();
        Forest secondCard = new Forest();
        Card thirdCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        Permanent jessica = addReadyJessica();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(jessica.isTapped()).isTrue();
        assertThat(jessica.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(topCard.getId(), secondCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCard.getId(), player1.getId())
                .containsEntry(secondCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(topCard.getId(), secondCard.getId());
    }

    private Permanent addReadyJessica() {
        Permanent jessica = harness.addToBattlefieldAndReturn(player1, new JessicaJonesPrivateEye());
        jessica.setSummoningSick(false);
        return jessica;
    }
}
