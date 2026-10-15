package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbradedBluffs.class})
class AbradedBluffsTest extends BaseCardTest {

    @Test
    void entersTappedAndDealsDamageToTargetOpponent() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbradedBluffs()));

        harness.playLand(player1, 0);

        Permanent bluffs = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bluffs.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void tappingProducesRedMana() {
        Permanent bluffs = addReadyBluffs();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bluffs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void tappingProducesWhiteMana() {
        Permanent bluffs = addReadyBluffs();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(bluffs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void enteringUnderOtherPlayersControlTargetsTheirOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bluffs = harness.enterBattlefieldAndReturn(player2, new AbradedBluffs());

        assertThat(bluffs.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void damageTriggerResolvesAfterLandLeavesBattlefield() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbradedBluffs()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent bluffs = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(bluffs.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void cannotProduceManaWhileTappedAfterEntering() {
        harness.setHand(player1, List.of(new AbradedBluffs()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    private Permanent addReadyBluffs() {
        Permanent bluffs = harness.addToBattlefieldAndReturn(player1, new AbradedBluffs());
        bluffs.setSummoningSick(false);
        return bluffs;
    }
}
