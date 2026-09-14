package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CeremonialGuard.class, FreshVolunteers.class})
class CeremonialGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking destroys Ceremonial Guard at end of combat")
    void attackingDestroysItAtEndOfCombat() {
        Permanent guard = addCreatureReady(player1, new CeremonialGuard());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(guard);
        harness.assertInGraveyard(player1, "Ceremonial Guard");
    }

    @Test
    @DisplayName("Blocking destroys Ceremonial Guard at end of combat")
    void blockingDestroysItAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);
        Permanent guard = addCreatureReady(player2, new CeremonialGuard());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(guard);
        harness.assertInGraveyard(player2, "Ceremonial Guard");
    }
}
