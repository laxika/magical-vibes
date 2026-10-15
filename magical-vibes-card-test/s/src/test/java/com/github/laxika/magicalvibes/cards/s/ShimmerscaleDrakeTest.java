package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimmerscaleDrake.class, Colossapede.class})
class ShimmerscaleDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ShimmerscaleDrake()));
        harness.setLibrary(player1, List.of(new Colossapede()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shimmerscale Drake");
        harness.assertInHand(player1, "Colossapede");
    }

    @Test
    @DisplayName("Cycling pays and discards immediately but draws only on resolution")
    void cyclingCostsArePaidBeforeDrawing() {
        ShimmerscaleDrake drake = new ShimmerscaleDrake();
        Colossapede first = new Colossapede();
        ShimmerscaleDrake second = new ShimmerscaleDrake();
        harness.setHand(player1, List.of(drake));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drake);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void insufficientManaDoesNotDiscardOrDraw() {
        ShimmerscaleDrake drake = new ShimmerscaleDrake();
        Colossapede draw = new Colossapede();
        harness.setHand(player1, List.of(drake));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drake);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertNotInGraveyard(player1, "Shimmerscale Drake");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying prevents ground creatures from blocking but allows flying blockers")
    void flyingRestrictsBlockers() {
        Permanent attacker = addCreatureReady(player1, new ShimmerscaleDrake());
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new ShimmerscaleDrake());
        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(bls.canBlockAttacker(gd, groundBlocker, attacker, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, attacker, defenders)).isTrue();
    }
}
