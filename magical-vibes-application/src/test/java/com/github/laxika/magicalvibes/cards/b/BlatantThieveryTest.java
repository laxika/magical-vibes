package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlatantThievery.class, ElvishWarrior.class})
class BlatantThieveryTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of one target permanent from the opponent")
    void gainsPermanentControlOfTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        cast(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Resolves with no effect when the opponent controls no permanent")
    void resolvesWithNoOpposingPermanent() {
        harness.castFromHand(player1, new BlatantThievery(), "{4}{U}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Requires a target when the opponent controls a permanent")
    void requiresTargetWhenOpponentControlsPermanent() {
        harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by its caster")
    void cannotTargetOwnPermanent() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose two permanents controlled by the same opponent")
    void cannotChooseTwoPermanentsFromSameOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(UUID targetId) {
        prepareCast();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new BlatantThievery()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
