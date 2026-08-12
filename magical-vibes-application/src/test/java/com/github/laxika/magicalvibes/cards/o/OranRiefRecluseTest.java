package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OranRiefRecluseTest extends BaseCardTest {

    @Test
    void entersWithoutKickerAndDoesNotDestroyFlyingCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new OranRiefRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Oran-Rief Recluse");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void destroysTargetFlyingCreatureWhenKicked() {
        harness.addToBattlefield(player2, new AirElemental());
        Permanent target = findPermanent(player2, "Air Elemental");
        harness.setHand(player1, List.of(new OranRiefRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Oran-Rief Recluse");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void cannotTargetCreatureWithoutFlyingWhenKicked() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new OranRiefRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }
}
