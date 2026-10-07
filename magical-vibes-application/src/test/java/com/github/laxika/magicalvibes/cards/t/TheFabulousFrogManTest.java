package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFabulousFrogMan.class, CloudSprite.class})
class TheFabulousFrogManTest extends BaseCardTest {

    @Test
    @DisplayName("The Fabulous Frog-Man can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new CloudSprite());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TheFabulousFrogMan());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Reach also allows blocking creatures without flying")
    void canBlockGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new TheFabulousFrogMan());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TheFabulousFrogMan());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "The Fabulous Frog-Man");
        harness.assertInGraveyard(player2, "The Fabulous Frog-Man");
    }
}
