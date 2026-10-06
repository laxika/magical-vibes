package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiningAerosaur.class, RaptorCompanion.class})
class ShiningAerosaurTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new ShiningAerosaur());
        addCreatureReady(player2, new RaptorCompanion());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new ShiningAerosaur());
        Permanent blocker = addCreatureReady(player2, new ShiningAerosaur());
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLife);
    }

    @Test
    void canBlockGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        Permanent blocker = addCreatureReady(player2, new ShiningAerosaur());
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLife);
    }
}
