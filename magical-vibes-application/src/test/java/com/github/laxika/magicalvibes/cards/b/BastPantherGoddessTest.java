package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BastPantherGoddess.class, GrizzlyBears.class})
class BastPantherGoddessTest extends BaseCardTest {

    @Test
    void cannotAttackOrBlockWithFewerThanThreeControlledCreatures() {
        Permanent bast = addCreatureReady(player1, new BastPantherGoddess());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(bast),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttackWhenControllingThreeCreatures() {
        Permanent bast = addCreatureReady(player1, new BastPantherGoddess());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bast.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBlockWhenControllingThreeCreatures() {
        Permanent bast = addCreatureReady(player1, new BastPantherGoddess());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(bast),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));

        assertThat(bast.isBlocking()).isTrue();
    }

    @Test
    void attackingCreatureGetsPlusXPlusXEqualToControlledCreatureCount() {
        addCreatureReady(player1, new BastPantherGoddess());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }
}
