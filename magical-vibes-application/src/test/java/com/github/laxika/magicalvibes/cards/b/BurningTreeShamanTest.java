package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.Gelectrode;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurningTreeShaman.class, Gelectrode.class, GruulSignet.class})
class BurningTreeShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its controller when they activate a non-mana ability")
    void controllerActivatingNonManaAbilityTakesDamage() {
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player1, new Gelectrode());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals 1 damage to an opponent who activates a non-mana ability")
    void opponentActivatingNonManaAbilityTakesDamage() {
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player2, new Gelectrode());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger for a mana ability")
    void manaAbilityDoesNotTrigger() {
        addCreatureReady(player1, new BurningTreeShaman());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        signet.setSummoningSick(false);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Burning-Tree Shaman triggers for the same non-mana activation")
    void eachCopyTriggersForOneActivation() {
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player2, new Gelectrode());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
