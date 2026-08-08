package com.github.laxika.magicalvibes.cards.j;

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

class JettingGlasskiteTest extends BaseCardTest {

    private UUID addGlasskite() {
        harness.addToBattlefield(player1, new JettingGlasskite());
        Permanent glasskite = findPermanent(player1, "Jetting Glasskite");
        glasskite.setSummoningSick(false);
        return glasskite.getId();
    }

    @Test
    @DisplayName("Counters the first spell that targets it each turn")
    void countersFirstSpellEachTurn() {
        UUID glasskiteId = addGlasskite();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, glasskiteId);

        // Lightning Bolt plus Jetting Glasskite's counter trigger on top.
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        harness.assertOnBattlefield(player1, "Jetting Glasskite");
        harness.assertInGraveyard(player2, "Lightning Bolt");

        Permanent glasskite = findPermanent(player1, "Jetting Glasskite");
        assertThat(glasskite.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Counters an activated ability that targets it")
    void countersTargetingAbility() {
        UUID glasskiteId = addGlasskite();

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = findPermanent(player2, "Icy Manipulator");
        icy.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icy), null, glasskiteId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        Permanent glasskite = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(glasskiteId)).findFirst().orElseThrow();
        assertThat(glasskite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counters its own controller's spell (no controller restriction)")
    void countersControllersOwnSpell() {
        UUID glasskiteId = addGlasskite();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, glasskiteId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        harness.assertOnBattlefield(player1, "Jetting Glasskite");
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("A second spell the same turn is not countered")
    void secondSpellSameTurnNotCountered() {
        UUID glasskiteId = addGlasskite();

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        // First bolt is countered.
        harness.castInstant(player2, 0, glasskiteId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Jetting Glasskite");

        // Second bolt the same turn: the trigger does not fire again — only the bolt is on the stack.
        harness.castInstant(player2, 0, glasskiteId);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");

        harness.passBothPriorities(); // resolve the bolt — 3 damage on a 4/4

        Permanent glasskite = findPermanent(player1, "Jetting Glasskite");
        assertThat(glasskite.getMarkedDamage()).isEqualTo(3);
    }
}
