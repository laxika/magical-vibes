package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BayouGroff.class, GrizzlyBears.class})
class BayouGroffTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost and enters the battlefield")
    void sacrificesCreatureAsAdditionalCost() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Bayou Groff");
    }

    @Test
    @DisplayName("Pays {3} instead of sacrificing and enters the battlefield")
    void paysManaInsteadOfSacrificing() {
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorceryWithSacrifice(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bayou Groff");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without a creature or enough mana for the alternate cost")
    void cannotCastWithoutCreatureOrMana() {
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
