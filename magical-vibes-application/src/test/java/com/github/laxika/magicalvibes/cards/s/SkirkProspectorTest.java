package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkirkProspector.class, GoblinSledder.class, ElvishWarrior.class})
class SkirkProspectorTest extends BaseCardTest {

    // ===== Mana ability behavior =====

    @Test
    @DisplayName("Sacrificing itself adds one red mana immediately (mana ability, no stack)")
    void sacrificeSelfAddsRedMana() {
        harness.addToBattlefield(player1, new SkirkProspector());

        harness.activateAbility(player1, 0, null, null);

        // Mana ability resolves immediately — no stack entry
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Skirk Prospector");
        harness.assertInGraveyard(player1, "Skirk Prospector");
    }

    @Test
    @DisplayName("Can sacrifice another Goblin to add red mana")
    void sacrificeAnotherGoblinAddsRedMana() {
        harness.addToBattlefield(player1, new SkirkProspector());
        harness.addToBattlefield(player1, new GoblinSledder());

        // With multiple Goblins, activating should prompt for a choice
        harness.activateAbility(player1, 0, null, null);
        UUID goblinSledderId = harness.getPermanentId(player1, "Goblin Sledder");
        harness.handlePermanentChosen(player1, goblinSledderId);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Goblin Sledder");
        // Skirk Prospector should still be on the battlefield
        harness.assertOnBattlefield(player1, "Skirk Prospector");
    }

    @Test
    @DisplayName("Can activate with summoning sickness since it's a mana ability without tap")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SkirkProspector());

        // Should succeed even though the creature just entered the battlefield
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Goblin creature")
    void cannotSacrificeNonGoblin() {
        harness.addToBattlefield(player1, new SkirkProspector());
        harness.addToBattlefield(player1, new ElvishWarrior());

        // With only one Goblin (Skirk itself), it should auto-sacrifice itself
        harness.activateAbility(player1, 0, null, null);

        // Skirk should be sacrificed (auto-selected as the only Goblin)
        harness.assertNotOnBattlefield(player1, "Skirk Prospector");
        // Elvish Warrior should still be on the battlefield
        harness.assertOnBattlefield(player1, "Elvish Warrior");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @CardUsed(BoggartShenanigans.class)
    @DisplayName("Can sacrifice a noncreature Goblin permanent")
    void sacrificeNoncreatureGoblinPermanent() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.addToBattlefield(player1, new SkirkProspector());

        UUID goblinEnchantmentId = harness.getPermanentId(player1, "Boggart Shenanigans");
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(goblinEnchantmentId);
        harness.handlePermanentChosen(player1, goblinEnchantmentId);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Boggart Shenanigans");
        harness.assertOnBattlefield(player1, "Skirk Prospector");
    }
}
