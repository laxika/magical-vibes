package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyromancersGogglesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the Goggles adds {R} and registers a pending red-spell copy")
    void activationAddsRedManaAndRegistersCopy() {
        Permanent goggles = addGoggles(player1);

        harness.activateAbility(player1, indexOf(player1, goggles), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.pendingNextRedInstantSorceryCopyCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a red instant with a pending copy puts a copy trigger on the stack")
    void copiesRedInstant() {
        gd.pendingNextRedInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(copyTriggers()).isEqualTo(1);
        assertThat(gd.pendingNextRedInstantSorceryCopyCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving the trigger puts a second copy of the spell on the stack")
    void copyTriggerCreatesSpellCopy() {
        gd.pendingNextRedInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve the copy trigger

        long boltCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Lightning Bolt"))
                .count();
        assertThat(boltCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("A non-red instant or sorcery does not consume or trigger the pending copy")
    void nonRedSpellIsNotCopied() {
        gd.pendingNextRedInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);

        assertThat(copyTriggers()).isZero();
        assertThat(gd.pendingNextRedInstantSorceryCopyCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature spell does not consume the pending copy")
    void creatureSpellIsNotCopied() {
        gd.pendingNextRedInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(copyTriggers()).isZero();
        assertThat(gd.pendingNextRedInstantSorceryCopyCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the first red spell is copied — the trigger is one-shot")
    void pendingCopyIsOneShot() {
        gd.pendingNextRedInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(copyTriggers()).isEqualTo(1);

        // The second bolt is cast with the pending copy already consumed — no new trigger.
        harness.castInstant(player1, 0, player2.getId());

        assertThat(copyTriggers()).isEqualTo(1);
    }

    @Test
    @DisplayName("The pending copy is cleared when mana pools drain")
    void pendingCopyClearedOnManaDrain() {
        gd.pendingNextRedInstantSorceryCopyCount.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.pendingNextRedInstantSorceryCopyCount).isEmpty();
    }

    private long copyTriggers() {
        return gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription() != null && e.getDescription().startsWith("Copy "))
                .count();
    }

    private Permanent addGoggles(Player player) {
        Permanent perm = new Permanent(new PyromancersGoggles());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
