package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuergarHedgeMage.class, Mountain.class, Plains.class, Ornithopter.class, RuleOfLaw.class})
class DuergarHedgeMageTest extends BaseCardTest {

    @Test
    @DisplayName("With two Mountains, ETB may destroy target artifact")
    void mountainsGateDestroysArtifact() {
        addLands(player1, 2, 0);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castDuergar();
        harness.passBothPriorities(); // resolve creature spell -> artifact target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Declining the Mountains trigger destroys nothing")
    void mountainsGateDeclinedDestroysNothing() {
        addLands(player1, 2, 0);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castDuergar();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("With only one Mountain the artifact trigger does not fire")
    void oneMountainDoesNotTrigger() {
        addLands(player1, 1, 0);
        harness.addToBattlefield(player2, new Ornithopter());
        castDuergar();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("With two Plains, ETB may destroy target enchantment")
    void plainsGateDestroysEnchantment() {
        addLands(player1, 0, 2);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities(); // artifact group skipped -> enchantment target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("With only one Plains the enchantment trigger does not fire")
    void onePlainsDoesNotTrigger() {
        addLands(player1, 0, 1);
        harness.addToBattlefield(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("With no Mountains or Plains, neither ability triggers")
    void neitherGateTriggers() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("With two Mountains and two Plains, both abilities may resolve")
    void bothGatesResolve() {
        addLands(player1, 2, 2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities(); // resolve creature spell -> first artifact target prompt

        harness.handlePermanentChosen(player1, artifact.getId());
        Permanent enchantment = findPermanent(player2, "Rule of Law");
        harness.handlePermanentChosen(player1, enchantment.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("Declining the Plains trigger preserves the enchantment")
    void plainsGateDeclinedDestroysNothing() {
        addLands(player1, 0, 2);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("Losing a Mountain before resolution prevents artifact destruction")
    void mountainsGateRecheckedAtResolution() {
        addLands(player1, 2, 0);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castDuergar();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Mountain"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Losing a Plains before resolution prevents enchantment destruction")
    void plainsGateRecheckedAtResolution() {
        addLands(player1, 0, 2);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchantment.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Plains"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("Opposing Mountains and Plains do not satisfy either condition")
    void opposingLandsDoNotEnableTriggers() {
        addLands(player2, 2, 2);
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("An enchantment ability with no legal target does not prevent artifact destruction")
    void missingEnchantmentTargetDoesNotSuppressArtifactTrigger() {
        addLands(player1, 2, 2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castDuergar();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("An artifact ability with no legal target does not prevent enchantment destruction")
    void missingArtifactTargetDoesNotSuppressEnchantmentTrigger() {
        addLands(player1, 2, 2);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, enchantment.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotOnBattlefield(player2, "Rule of Law");
    }

    private void castDuergar() {
        harness.setHand(player1, List.of(new DuergarHedgeMage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
    }

    private void addLands(Player player, int mountains, int plains) {
        for (int i = 0; i < mountains; i++) {
            harness.addToBattlefield(player, new Mountain());
        }
        for (int i = 0; i < plains; i++) {
            harness.addToBattlefield(player, new Plains());
        }
    }

}
