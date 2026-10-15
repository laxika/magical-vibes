package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeaverOfCurrents.class})
class WeaverOfCurrentsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps and adds {C}{C}")
    void activateAbilityAddsTwoColorless() {
        Permanent weaver = addCreatureReady(player1, new WeaverOfCurrents());

        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        assertThat(weaver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore + 2);
        assertThat(gd.stack).isEmpty(); // mana ability resolves immediately
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new WeaverOfCurrents());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new WeaverOfCurrents());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can produce another two colorless mana after untapping")
    void canActivateAgainAfterUntapping() {
        Permanent weaver = addCreatureReady(player1, new WeaverOfCurrents());
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);
        harness.performUntapStep(player1);
        assertThat(weaver.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(weaver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(manaBefore + 4);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
