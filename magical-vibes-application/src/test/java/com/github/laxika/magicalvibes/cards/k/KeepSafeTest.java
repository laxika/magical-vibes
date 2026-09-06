package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeepSafe.class, Shock.class, GrizzlyBears.class})
class KeepSafeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell targeting a permanent you control and draws a card")
    void countersSpellTargetingControlledPermanentAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new KeepSafe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Cannot target a spell targeting an opponent's permanent")
    void cannotTargetSpellTargetingOpponentsPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new KeepSafe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
