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

@CardUsed({ScuttlingSliver.class, WingedSliver.class, GrizzlyBears.class})
class ScuttlingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Scuttling Sliver can pay to untap itself")
    void untapsItself() {
        Permanent scuttling = addCreatureReady(player1, new ScuttlingSliver());
        scuttling.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scuttling.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Scuttling Sliver grants its ability to other Slivers you control")
    void grantsAbilityToOtherSliversYouControl() {
        addCreatureReady(player1, new ScuttlingSliver());
        Permanent sliver = addCreatureReady(player1, new WingedSliver());
        sliver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(sliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only Slivers you control gain Scuttling Sliver's ability")
    void doesNotGrantAbilityOutsideControlledSlivers() {
        addCreatureReady(player1, new ScuttlingSliver());
        Permanent nonSliver = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingSliver = addCreatureReady(player2, new WingedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
