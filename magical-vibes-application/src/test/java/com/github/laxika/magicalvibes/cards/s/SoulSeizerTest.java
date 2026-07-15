package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulSeizerTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent soulSeizer = addReadyCreature(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may and choosing a creature transforms Soul Seizer and steals the target")
    void transformAndAttachStealsTarget() {
        Permanent soulSeizer = addReadyCreature(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(soulSeizer.isTransformed()).isTrue();
        assertThat(soulSeizer.getCard().getName()).isEqualTo("Ghastly Haunting");
        assertThat(soulSeizer.isAttached()).isTrue();
        assertThat(soulSeizer.getAttachedTo()).isEqualTo(bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.stolenCreatures).containsEntry(bears.getId(), player2.getId());
    }

    @Test
    @DisplayName("Declining the may ability leaves Soul Seizer unchanged")
    void declineTransform() {
        Permanent soulSeizer = addReadyCreature(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(soulSeizer.isTransformed()).isFalse();
        assertThat(soulSeizer.getCard().getName()).isEqualTo("Soul Seizer");
        assertThat(soulSeizer.isAttached()).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No trigger when defender has no creatures")
    void noTriggerWhenNoCreatures() {
        Permanent soulSeizer = addReadyCreature(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when Soul Seizer is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        Permanent soulSeizer = addReadyCreature(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting may presents only the damaged player's creatures as attach targets")
    void onlyDamagedPlayerCreaturesAsTargets() {
        Permanent soulSeizer = addReadyCreature(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent ownBears = addReadyCreature(player1, new GrizzlyBears());
        Permanent enemyBears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyBears.getId())
                .doesNotContain(ownBears.getId());
    }

    @Test
    @DisplayName("Game advances after attach target is chosen")
    void gameAdvancesAfterAttachChoice() {
        Permanent soulSeizer = addReadyCreature(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
