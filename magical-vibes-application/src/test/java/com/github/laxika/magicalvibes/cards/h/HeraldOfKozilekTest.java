package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KozileksSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfKozilek.class, KozileksSentinel.class, GrizzlyBears.class})
class HeraldOfKozilekTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless spells you cast cost {1} less to cast")
    void colorlessSpellsCostOneLess() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player1, List.of(new KozileksSentinel()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Colored spells are not reduced")
    void coloredSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to an opponent's colorless spells")
    void opponentColorlessSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player2, List.of(new KozileksSentinel()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
