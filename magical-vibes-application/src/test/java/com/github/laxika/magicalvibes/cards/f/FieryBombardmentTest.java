package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DeepSlumberTitan;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieryBombardmentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the red mana symbols in the sacrificed creature's cost")
    void dealsDamageEqualToRedSymbols() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new DeepSlumberTitan()); // {2}{R}{R} -> 2 red symbols
        harness.addToBattlefield(player1, new GrizzlyBears()); // second creature forces a choice
        UUID giant = harness.getPermanentId(player1, "Deep-Slumber Titan");
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, giant);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Deep-Slumber Titan"); // sacrificed as cost
    }

    @Test
    @DisplayName("A single red pip deals 1 damage")
    void singleRedPipDealsOne() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new HillGiant()); // {3}{R} -> 1 red symbol (only creature: auto-sacrificed)
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrificing a creature with no red pips deals no damage")
    void noRedPipsDealsNoDamage() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G} -> 0 red symbols (only creature: auto-sacrificed)
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without paying the {2} cost")
    void requiresMana() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // 1 short

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
