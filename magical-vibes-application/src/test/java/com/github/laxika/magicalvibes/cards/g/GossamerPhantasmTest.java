package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.cards.r.RathiTrapper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GossamerPhantasm.class, Ovinize.class, RathiTrapper.class})
class GossamerPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());

        harness.setHand(player2, List.of(new Ovinize()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, phantasm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());
        Permanent rathiTrapper = addCreatureReady(player2, new RathiTrapper());

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(rathiTrapper),
                null, phantasm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Stays on the battlefield when it is not targeted")
    void staysWhenNotTargeted() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new RathiTrapper());

        harness.setHand(player2, List.of(new Ovinize()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(phantasm.getId()));
    }
}
