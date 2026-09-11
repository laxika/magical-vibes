package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfEssence.class, WarriorEnKor.class, Shock.class, SpinedWurm.class})
class WallOfEssenceTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to combat damage dealt to Wall of Essence")
    void gainsLifeFromCombatDamage() {
        addCreatureReady(player2, new WallOfEssence());
        addCreatureReady(player1, new WarriorEnKor());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Wall of Essence");
    }

    @Test
    @DisplayName("Combat damage trigger resolves even if lethal damage destroys Wall of Essence")
    void gainsLifeWhenLethalCombatDamageDestroysIt() {
        Permanent wall = addCreatureReady(player2, new WallOfEssence());
        addCreatureReady(player1, new SpinedWurm());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 5);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wall);
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Wall of Essence")
    void ignoresNoncombatDamage() {
        harness.addToBattlefield(player2, new WallOfEssence());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Wall of Essence"));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
