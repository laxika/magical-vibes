package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(JaggedBarrens.class)
class JaggedBarrensTest extends BaseCardTest {

    @Test
    void entersTappedAndDealsDamageToTargetOpponent() {
        harness.setHand(player1, List.of(new JaggedBarrens()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void enteringWithoutBeingPlayedStillTriggersDamage() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new JaggedBarrens());

        assertThat(land.isTapped()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void entryTriggerResolvesAfterSourceLeavesBattlefield() {
        harness.setHand(player1, List.of(new JaggedBarrens()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent land = gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.setGraveyard(player1, List.of(land.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void tappingProducesBlackMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void tappingProducesRedMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new JaggedBarrens());
        land.setSummoningSick(false);
        return land;
    }
}
