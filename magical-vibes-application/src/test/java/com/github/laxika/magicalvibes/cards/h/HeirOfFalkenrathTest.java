package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeirOfFalkenrathTest extends BaseCardTest {

    @Test
    void discardingACardTransformsHeir() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, indexOf(heir), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(heir.isTransformed()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(heir), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heir.isTransformed()).isFalse();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
