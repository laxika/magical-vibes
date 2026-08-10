package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DuskworkerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked grants Duskworker one regeneration shield")
    void becomingBlockedGrantsRegenerationShield() {
        Permanent duskworker = addReadyDuskworker(player1);
        duskworker.setAttacking(true);
        addCreatureReady(player2, 5, 5);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(duskworker.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Three generic mana gives Duskworker +1/+0 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent duskworker = addReadyDuskworker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(duskworker.getPowerModifier()).isEqualTo(1);
        assertThat(duskworker.getToughnessModifier()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(duskworker.getPowerModifier()).isZero();
    }

    private Permanent addReadyDuskworker(Player player) {
        Permanent perm = new Permanent(new Duskworker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
