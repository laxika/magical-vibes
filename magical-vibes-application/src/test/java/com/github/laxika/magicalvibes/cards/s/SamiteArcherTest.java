package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SamiteArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage to a target creature")
    void preventsNextDamageToTargetCreature() {
        Permanent archer = addReadyArcher(player1);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        addReadySpellcaster(player1);

        harness.activateAbility(player1, 0, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(archer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents the next damage to a target player")
    void preventsNextDamageToTargetPlayer() {
        addReadyArcher(player1);
        addReadySpellcaster(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals 1 damage to a target player")
    void dealsDamageToTargetPlayer() {
        addReadyArcher(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyArcher(Player player) {
        Permanent archer = new Permanent(new SamiteArcher());
        archer.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(archer);
        return archer;
    }

    private void addReadySpellcaster(Player player) {
        Permanent spellcaster = new Permanent(new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(spellcaster);
    }
}
