package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VraskasFinisher.class, GarrukWildspeaker.class, GrizzlyBears.class})
class VraskasFinisherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a damaged creature an opponent controls")
    void etbDestroysDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castFinisher(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB destroys a damaged planeswalker an opponent controls")
    void etbDestroysDamagedPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 3);
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castFinisher(target);

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Cannot target an opponent creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VraskasFinisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    private void castFinisher(Permanent target) {
        harness.setHand(player1, List.of(new VraskasFinisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
