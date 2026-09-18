package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnrollInTheCoalition.class, ProdigalPyromancer.class, Shock.class})
class EnrollInTheCoalitionTest extends BaseCardTest {

    @Test
    void controllerIsAFlagbearerForOpponentSpellTargets() {
        addEnrollToBattlefield();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void controllerIsAFlagbearerForOpponentActivatedAbilityTargets() {
        addEnrollToBattlefield();
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        int pyromancerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer);

        assertThatThrownBy(() -> harness.activateAbility(player2, pyromancerIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.activateAbility(player2, pyromancerIndex, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    private void addEnrollToBattlefield() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new EnrollInTheCoalition()));
    }
}
