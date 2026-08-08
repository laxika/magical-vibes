package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShimmeringGlasskiteTest extends BaseCardTest {

    private UUID addGlasskite() {
        harness.addToBattlefield(player1, new ShimmeringGlasskite());
        Permanent kite = findPermanent(player1, "Shimmering Glasskite");
        kite.setSummoningSick(false);
        return kite.getId();
    }

    @Test
    @DisplayName("Counters the first spell that targets it each turn")
    void countersFirstSpellEachTurn() {
        UUID kiteId = addGlasskite();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, kiteId);

        // Lightning Bolt plus the counter trigger on top.
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        harness.assertOnBattlefield(player1, "Shimmering Glasskite");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("Counters an activated ability that targets it")
    void countersTargetingAbility() {
        UUID kiteId = addGlasskite();

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = findPermanent(player2, "Icy Manipulator");
        icy.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icy), null, kiteId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        Permanent kite = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(kiteId)).findFirst().orElseThrow();
        assertThat(kite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A second spell the same turn is not countered")
    void secondSpellSameTurnNotCountered() {
        UUID kiteId = addGlasskite();

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, kiteId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shimmering Glasskite");

        // Second bolt the same turn: the trigger does not fire again.
        harness.castInstant(player2, 0, kiteId);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // 3 damage is lethal to the 2/3

        harness.assertNotOnBattlefield(player1, "Shimmering Glasskite");
        harness.assertInGraveyard(player1, "Shimmering Glasskite");
    }
}
