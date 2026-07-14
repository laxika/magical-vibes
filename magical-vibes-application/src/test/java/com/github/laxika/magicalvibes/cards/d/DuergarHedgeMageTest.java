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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DuergarHedgeMageTest extends BaseCardTest {

    // ===== Mountains gate: may destroy target artifact =====

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

        assertThat(onBattlefield(player2, "Ornithopter")).isFalse();
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

        assertThat(onBattlefield(player2, "Ornithopter")).isTrue();
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
        assertThat(onBattlefield(player2, "Ornithopter")).isTrue();
    }

    // ===== Plains gate: may destroy target enchantment =====

    @Test
    @DisplayName("With two Plains, ETB may destroy target enchantment")
    void plainsGateDestroysEnchantment() {
        addLands(player1, 0, 2);
        harness.addToBattlefield(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities(); // artifact group skipped -> enchantment target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent enchantment = battlefieldPermanent(player2, "Rule of Law");
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(onBattlefield(player2, "Rule of Law")).isFalse();
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
        assertThat(onBattlefield(player2, "Rule of Law")).isTrue();
    }

    // ===== Neither gate met =====

    @Test
    @DisplayName("With no Mountains or Plains, neither ability triggers")
    void neitherGateTriggers() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(onBattlefield(player2, "Ornithopter")).isTrue();
        assertThat(onBattlefield(player2, "Rule of Law")).isTrue();
    }

    // ===== Both gates met: independent artifact + enchantment destruction =====

    @Test
    @DisplayName("With two Mountains and two Plains, both abilities may resolve")
    void bothGatesResolve() {
        addLands(player1, 2, 2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        castDuergar();
        harness.passBothPriorities(); // resolve creature spell -> first artifact target prompt

        harness.handlePermanentChosen(player1, artifact.getId());
        Permanent enchantment = battlefieldPermanent(player2, "Rule of Law");
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities(); // resolve bundled ETB -> first may prompt (artifact)
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true); // second may prompt (enchantment)

        assertThat(onBattlefield(player2, "Ornithopter")).isFalse();
        assertThat(onBattlefield(player2, "Rule of Law")).isFalse();
    }

    // ===== Helpers =====

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

    private boolean onBattlefield(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals(cardName));
    }

    private Permanent battlefieldPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
