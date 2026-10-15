package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimekinRecluse.class, GrizzlyBears.class, FountainOfYouth.class})
class RimekinRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one other target creature to its owner's hand")
    void etbReturnsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castRimekinRecluse(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Rimekin Recluse");
    }

    @Test
    @DisplayName("ETB resolves with no target")
    void etbResolvesWithNoTarget() {
        harness.castFromHand(player1, new RimekinRecluse(), "{2}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rimekin Recluse");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void etbRejectsNoncreatureTarget() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.setHand(player1, List.of(new RimekinRecluse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    @Test
    @DisplayName("ETB may leave another available creature on the battlefield")
    void etbMayChooseNoTargetWithCreatureAvailable() {
        harness.addToBattlefield(player2, new RimekinRecluse());
        harness.castFromHand(player1, new RimekinRecluse(), "{2}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rimekin Recluse");
        harness.assertOnBattlefield(player2, "Rimekin Recluse");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB may return another Recluse controlled by its controller")
    void etbReturnsAnotherRecluseYouControl() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RimekinRecluse()).getId();
        castRimekinRecluse(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        harness.assertInHand(player1, "Rimekin Recluse");
        harness.assertOnBattlefield(player1, "Rimekin Recluse");
    }

    private void castRimekinRecluse(UUID targetId) {
        harness.setHand(player1, List.of(new RimekinRecluse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, List.of(targetId));
    }
}
