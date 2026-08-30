package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurningTreeShaman.class, LlanowarElves.class, ProdigalPyromancer.class})
class BurningTreeShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its controller when they activate a non-mana ability")
    void controllerActivatingNonManaAbilityTakesDamage() {
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals 1 damage to an opponent who activates a non-mana ability")
    void opponentActivatingNonManaAbilityTakesDamage() {
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger for a mana ability")
    void manaAbilityDoesNotTrigger() {
        addCreatureReady(player1, new BurningTreeShaman());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());
        harness.setLife(player2, 20);
        int elvesIndex = gd.playerBattlefields.get(player2.getId()).indexOf(elves);

        harness.tapPermanent(player2, elvesIndex);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
