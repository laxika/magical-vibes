package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShieldOfTheRighteousTest extends BaseCardTest {

    // ===== Static boost + vigilance =====

    @Test
    @DisplayName("Equipped creature gets +0/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1); // Grizzly Bears 2/2
        Permanent shield = addShield(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield = addShield(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature gets no boost or vigilance")
    void unequippedCreatureNoBoost() {
        Permanent creature = addReadyCreature(player1);
        addShield(player1); // not attached

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Block trigger =====

    @Test
    @DisplayName("When equipped creature blocks, a trigger auto-targeting the blocked attacker is created")
    void blockingCreatesTrigger() {
        Permanent blocker = addReadyCreature(player2);
        Permanent shield = addShield(player2);
        shield.setAttachedTo(blocker.getId());

        Permanent attacker = addReadyAttacker(player1);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Shield of the Righteous")
                        && se.isNonTargeting()
                        && se.getTargetId().equals(attacker.getId())
                        && se.getSourcePermanentId().equals(shield.getId()));
    }

    @Test
    @DisplayName("Resolving the block trigger sets skipUntapCount on the blocked attacker")
    void resolvingSetsSkipUntapCount() {
        Permanent blocker = addReadyCreature(player2);
        Permanent shield = addShield(player2);
        shield.setAttachedTo(blocker.getId());

        Permanent attacker = addReadyAttacker(player1);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("No trigger when the Shield is not attached to any creature")
    void noTriggerWhenNotEquipped() {
        addReadyCreature(player2);
        addShield(player2); // on battlefield but unattached

        addReadyAttacker(player1);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Shield of the Righteous"));
    }

    // ===== Helpers =====

    private Permanent addShield(Player player) {
        Permanent perm = new Permanent(new ShieldOfTheRighteous());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
