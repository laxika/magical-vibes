package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinSharpshooter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tephraderm.class, Shock.class, GoblinSharpshooter.class})
class TephradermTest extends BaseCardTest {

    @Test
    @DisplayName("Spell damage makes Tephraderm deal that much damage to the spell's controller")
    void spellDamageHitsSpellController() {
        Permanent tephraderm = harness.addToBattlefieldAndReturn(player2, new Tephraderm());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, tephraderm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(tephraderm.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature damage makes Tephraderm deal that much damage back to the creature")
    void creatureDamageHitsDamagingCreature() {
        addCreatureReady(player1, new GoblinSharpshooter());
        Permanent tephraderm = addCreatureReady(player2, new Tephraderm());

        harness.activateAbility(player1, 0, null, tephraderm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Sharpshooter");
        harness.assertOnBattlefield(player2, "Tephraderm");
        assertThat(tephraderm.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage makes Tephraderm deal that much damage back to the creature")
    void combatDamageHitsDamagingCreature() {
        addCreatureReady(player1, new GoblinSharpshooter());
        Permanent tephraderm = addCreatureReady(player2, new Tephraderm());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        // Check before the automatic progression reaches cleanup, which clears marked damage.
        assertThat(tephraderm.getMarkedDamage()).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Sharpshooter");
        harness.assertOnBattlefield(player2, "Tephraderm");
    }
}
