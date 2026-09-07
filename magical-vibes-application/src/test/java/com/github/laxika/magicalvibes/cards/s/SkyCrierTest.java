package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyCrier.class, GrizzlyBears.class, FountainOfYouth.class})
class SkyCrierTest extends BaseCardTest {

    @Test
    @DisplayName("Sky Crier's ability makes you and target opponent draw a card")
    void eachPlayerDraws() {
        Permanent skyCrier = addReadySkyCrier(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new FountainOfYouth()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(skyCrier.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Fountain of Youth");
    }

    @Test
    @DisplayName("Sky Crier's ability cannot target its controller")
    void abilityCannotTargetController() {
        addReadySkyCrier(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private Permanent addReadySkyCrier(Player player) {
        Permanent skyCrier = new Permanent(new SkyCrier());
        skyCrier.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(skyCrier);
        return skyCrier;
    }
}
