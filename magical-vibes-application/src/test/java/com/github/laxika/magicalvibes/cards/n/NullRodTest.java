package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.PhyrexianFurnace;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullRod.class, MindStone.class, PhyrexianFurnace.class, WindingCanyons.class})
class NullRodTest extends BaseCardTest {

    @Test
    void preventsArtifactManaAbilities() {
        addNullRod(player1);
        harness.addToBattlefield(player2, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void preventsNonManaArtifactAbilities() {
        addNullRod(player1);
        harness.addToBattlefield(player2, new PhyrexianFurnace());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void doesNotPreventLandManaAbilities() {
        addNullRod(player1);
        harness.addToBattlefield(player2, new WindingCanyons());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void doesNotPreventNonManaAbilitiesOfLands() {
        addNullRod(player1);
        harness.addToBattlefield(player2, new WindingCanyons());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void preventsOwnArtifactAbilities() {
        addNullRod(player1);
        harness.addToBattlefield(player1, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void removingNullRodReenablesArtifactAbilities() {
        Permanent nullRod = harness.addToBattlefieldAndReturn(player1, new NullRod());
        harness.addToBattlefield(player2, new MindStone());

        gd.playerBattlefields.get(player1.getId()).remove(nullRod);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void addNullRod(Player player) {
        harness.addToBattlefield(player, new NullRod());
    }
}
