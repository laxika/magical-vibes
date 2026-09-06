package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
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

@CardUsed({BondersEnclave.class, ColossalDreadmaw.class, GrizzlyBears.class})
class BondersEnclaveTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(enclave.isTapped()).isTrue();
    }

    @Test
    void drawsCardWhenControllingCreatureWithPowerFourOrGreater() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        Card drawn = new GrizzlyBears();
        setDeck(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, indexOf(enclave), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(enclave.isTapped()).isTrue();
    }

    @Test
    void cannotDrawWithoutControllingCreatureWithPowerFourOrGreater() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enclave), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a creature with power 4 or greater");
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
