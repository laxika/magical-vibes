package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WingedSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScuttlingSliver.class, WingedSliver.class, GrizzlyBears.class, SyphonSliver.class})
class ScuttlingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Scuttling Sliver can pay {2} to untap itself")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new ScuttlingSliver());
        Permanent scuttlingSliver = gd.playerBattlefields.get(player1.getId()).getFirst();
        scuttlingSliver.setSummoningSick(false);
        scuttlingSliver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scuttlingSliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Scuttling Sliver grants the untap ability to other Slivers you control")
    void grantsAbilityToOtherSlivers() {
        harness.addToBattlefield(player1, new ScuttlingSliver());
        harness.addToBattlefield(player1, new SyphonSliver());
        Permanent syphonSliver = gd.playerBattlefields.get(player1.getId()).get(1);
        syphonSliver.setSummoningSick(false);
        syphonSliver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(syphonSliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Scuttling Sliver does not grant the ability to non-Slivers")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new ScuttlingSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Scuttling Sliver does not grant the ability to an opponent's Slivers")
    void doesNotGrantAbilityToOpponentSlivers() {
        harness.addToBattlefield(player1, new ScuttlingSliver());
        harness.addToBattlefield(player2, new SyphonSliver());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
