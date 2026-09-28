package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarRoom.class, EdgarMarkov.class, GrizzlyBears.class})
class WarRoomTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        Permanent warRoom = harness.addToBattlefieldAndReturn(player1, new WarRoom());

        harness.activateAbility(player1, indexOf(warRoom), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(warRoom.isTapped()).isTrue();
    }

    @Test
    void drawsAndPaysForEachCommanderIdentityColor() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        Permanent warRoom = harness.addToBattlefieldAndReturn(player1, new WarRoom());
        Card drawn = new GrizzlyBears();
        setDeck(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, indexOf(warRoom), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(warRoom.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void cannotActivateWhenLifeIsLessThanCommanderIdentityCost() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        Permanent warRoom = harness.addToBattlefieldAndReturn(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(warRoom), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life to pay");

        assertThat(warRoom.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
