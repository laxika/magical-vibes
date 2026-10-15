package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AardvarkSloth.class)
class AardvarkSlothTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent sloth = addCreatureReady(player1, new AardvarkSloth());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sloth)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Both sloths gain life when they deal lethal combat damage to each other")
    void lifelinkGainsLifeForBothControllersBeforeCreaturesDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AardvarkSloth());
        addCreatureReady(player2, new AardvarkSloth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertInGraveyard(player1, "Aardvark Sloth");
        harness.assertInGraveyard(player2, "Aardvark Sloth");
        harness.assertNotOnBattlefield(player1, "Aardvark Sloth");
        harness.assertNotOnBattlefield(player2, "Aardvark Sloth");
    }
}
