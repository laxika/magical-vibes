package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeidarRimewindMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target permanent to its owner's hand with four snow permanents")
    void returnsTargetPermanentWithFourSnowPermanents() {
        addCreatureReady(player1, new HeidarRimewindMaster());
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without four snow permanents you control")
    void cannotActivateWithoutFourSnowPermanentsYouControl() {
        addCreatureReady(player1, new HeidarRimewindMaster());
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        addSnowPermanent(player2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more snow permanents");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new HeidarRimewindMaster());
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSnowPermanent(Player player) {
        Permanent snow = new Permanent(new GrizzlyBears());
        TestCards.mutableCard(snow).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snow);
    }
}
