package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsPrey.class, DragonWhelp.class, GrizzlyBears.class})
class DragonsPreyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a non-Dragon creature for its normal cost")
    void destroysNonDragonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Costs two more when targeting a Dragon")
    void costsMoreWhenTargetingDragon() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Dragon Whelp");
    }

    @Test
    @DisplayName("Destroys a Dragon when the additional cost is paid")
    void destroysDragonWithAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dragon Whelp");
    }
}
