package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.k.KithkinShielddare;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HungrySpriggan.class, KithkinShielddare.class})
class HungrySprigganTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(3);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Trample deals excess combat damage after its attack boost resolves")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());
        Permanent blocker = addCreatureReady(player2, new KithkinShielddare());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertLife(player2, 17);
        assertThat(spriggan.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(3);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(spriggan.getPowerModifier()).isEqualTo(0);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(0);
    }
}
