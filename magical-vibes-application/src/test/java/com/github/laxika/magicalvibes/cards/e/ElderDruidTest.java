package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElderDruidTest extends BaseCardTest {

    // ===== Tap branch =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        addReadyDruid(player1);
        Permanent target = addReadyCreature(player2);
        addDruidMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Elder Druid")
    void activatingTapsDruid() {
        Permanent druid = addReadyDruid(player1);
        Permanent target = addReadyCreature(player2);
        addDruidMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(druid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving taps an untapped target creature")
    void resolvingTapsUntappedCreature() {
        addReadyDruid(player1);
        Permanent target = addReadyCreature(player2);
        addDruidMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    // ===== Untap branch =====

    @Test
    @DisplayName("Resolving untaps a tapped target creature")
    void resolvingUntapsTappedCreature() {
        addReadyDruid(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        addDruidMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap a tapped own land")
    void canUntapOwnLand() {
        addReadyDruid(player1);
        Permanent ownLand = addReadyLand(player1);
        ownLand.tap();
        addDruidMana(player1);

        harness.activateAbility(player1, 0, null, ownLand.getId());
        harness.passBothPriorities();

        assertThat(ownLand.isTapped()).isFalse();
    }

    // ===== Target types =====

    @Test
    @DisplayName("Can tap target artifact")
    void canTapTargetArtifact() {
        addReadyDruid(player1);
        Permanent targetArtifact = addReadyArtifact(player2);
        addDruidMana(player1);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyDruid(player1);
        Permanent enchantment = addReadyEnchantment(player2);
        addDruidMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    // ===== Summoning sickness (creature source) =====

    @Test
    @DisplayName("Cannot activate the turn it enters (summoning sickness applies to a creature's tap ability)")
    void summoningSickCannotActivate() {
        ElderDruid card = new ElderDruid();
        Permanent druid = new Permanent(card);
        druid.setSummoningSick(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(druid);

        Permanent target = addReadyCreature(player2);
        addDruidMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addReadyDruid(player1);
        Permanent target = addReadyCreature(player2);
        addDruidMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyDruid(player1);
        Permanent target = addReadyCreature(player2);
        addDruidMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private void addDruidMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyDruid(Player player) {
        ElderDruid card = new ElderDruid();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Forest card = new Forest();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        AngelsFeather card = new AngelsFeather();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyEnchantment(Player player) {
        Pacifism card = new Pacifism();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
