package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtherworldAtlasTest extends BaseCardTest {

    @Test
    @DisplayName("First ability puts a charge counter on Otherworld Atlas")
    void tapAddsChargeCounter() {
        Permanent atlas = addReadyAtlas(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(atlas.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(atlas.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability makes each player draw a card per charge counter")
    void eachPlayerDrawsPerChargeCounter() {
        Permanent atlas = addReadyAtlas(player1);
        atlas.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 3);
    }

    @Test
    @DisplayName("Second ability draws nothing with no charge counters")
    void noCountersDrawsNothing() {
        addReadyAtlas(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Abilities require tapping, so a tapped Atlas cannot activate")
    void tappedAtlasCannotActivate() {
        Permanent atlas = addReadyAtlas(player1);
        atlas.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAtlas(Player player) {
        Permanent perm = new Permanent(new OtherworldAtlas());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
