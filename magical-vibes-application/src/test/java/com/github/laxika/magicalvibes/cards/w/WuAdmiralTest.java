package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WuAdmiralTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/3 when no opponent controls an Island")
    void baseStatsWithoutOpponentIsland() {
        harness.addToBattlefield(player1, new WuAdmiral());

        Permanent admiral = findAdmiral();
        assertThat(gqs.getEffectivePower(gd, admiral)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, admiral)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 becoming 4/4 when an opponent controls an Island")
    void boostedWhenOpponentControlsIsland() {
        harness.addToBattlefield(player1, new WuAdmiral());
        harness.addToBattlefield(player2, new Island());

        Permanent admiral = findAdmiral();
        assertThat(gqs.getEffectivePower(gd, admiral)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, admiral)).isEqualTo(4);
    }

    @Test
    @DisplayName("Controller's own Island does NOT grant the boost")
    void ownIslandDoesNotBoost() {
        harness.addToBattlefield(player1, new WuAdmiral());
        harness.addToBattlefield(player1, new Island());

        Permanent admiral = findAdmiral();
        assertThat(gqs.getEffectivePower(gd, admiral)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, admiral)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Island opponent land does NOT grant the boost")
    void opponentNonIslandDoesNotBoost() {
        harness.addToBattlefield(player1, new WuAdmiral());
        harness.addToBattlefield(player2, new Forest());

        Permanent admiral = findAdmiral();
        assertThat(gqs.getEffectivePower(gd, admiral)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, admiral)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses boost when opponent's Island leaves the battlefield")
    void losesBoostWhenIslandLeaves() {
        harness.addToBattlefield(player1, new WuAdmiral());
        harness.addToBattlefield(player2, new Island());

        Permanent admiral = findAdmiral();
        assertThat(gqs.getEffectivePower(gd, admiral)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Island"));

        assertThat(gqs.getEffectivePower(gd, admiral)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, admiral)).isEqualTo(3);
    }

    private Permanent findAdmiral() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wu Admiral"))
                .findFirst().orElseThrow();
    }
}
