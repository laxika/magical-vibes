package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NiblisOfTheMist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverclawGriffin.class, NiblisOfTheMist.class, ScreechingSkaab.class})
class SilverclawGriffinTest extends BaseCardTest {

    @Test
    void nonFlyingCreatureCannotBlockGriffin() {
        addCreatureReady(player1, new SilverclawGriffin());
        addCreatureReady(player2, new ScreechingSkaab());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void firstStrikeKillsFlyingBlockerBeforeItCanDealDamage() {
        Permanent griffin = addCreatureReady(player1, new SilverclawGriffin());
        addCreatureReady(player2, new NiblisOfTheMist());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Silverclaw Griffin");
        assertThat(griffin.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Niblis of the Mist");
        harness.assertNotOnBattlefield(player2, "Niblis of the Mist");
        harness.assertLife(player2, 20);
    }

    @Test
    void griffinCanBlockGroundAttackerAndKillItBeforeRegularDamage() {
        addCreatureReady(player1, new ScreechingSkaab());
        Permanent griffin = addCreatureReady(player2, new SilverclawGriffin());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Silverclaw Griffin");
        assertThat(griffin.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Screeching Skaab");
        harness.assertNotOnBattlefield(player1, "Screeching Skaab");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedGriffinDealsDamageOnlyOnce() {
        addCreatureReady(player1, new SilverclawGriffin());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}

