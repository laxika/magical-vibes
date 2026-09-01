package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GossamerPhantasm.class, Shock.class, IcyManipulator.class})
class GossamerPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, phantasm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icyManipulator.setSummoningSick(false);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator),
                null, phantasm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Stays on the battlefield when it is not targeted")
    void staysWhenNotTargeted() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(phantasm.getId()));
    }
}
