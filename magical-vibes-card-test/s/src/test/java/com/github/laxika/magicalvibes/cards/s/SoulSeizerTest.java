package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DrogskolCaptain;
import com.github.laxika.magicalvibes.cards.n.NiblisOfTheMist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulSeizer.class, NiblisOfTheMist.class, DrogskolCaptain.class})
class SoulSeizerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent target = addCreatureReady(player2, new NiblisOfTheMist());

        resolveCombat();
        chooseTriggerTarget(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may and choosing a creature transforms Soul Seizer and steals the target")
    void transformAndAttachStealsTarget() {
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new NiblisOfTheMist());

        resolveCombat();

        chooseTriggerTarget(bears);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(soulSeizer.isTransformed()).isTrue();
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
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent target = addCreatureReady(player2, new NiblisOfTheMist());

        resolveCombat();
        chooseTriggerTarget(target);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(soulSeizer.isTransformed()).isFalse();
        assertThat(soulSeizer.isAttached()).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No trigger when defender has no creatures")
    void noTriggerWhenNoCreatures() {
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when Soul Seizer is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NiblisOfTheMist());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Trigger targets only creatures controlled by the damaged player")
    void onlyDamagedPlayerCreaturesAsTargets() {
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent ownBears = addCreatureReady(player1, new NiblisOfTheMist());
        Permanent enemyBears = addCreatureReady(player2, new NiblisOfTheMist());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enemyBears.getId())
                .doesNotContain(ownBears.getId());
    }

    @Test
    @DisplayName("Game advances after attach target is chosen")
    void gameAdvancesAfterAttachChoice() {
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new NiblisOfTheMist());

        resolveCombat();

        chooseTriggerTarget(bears);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Hexproof creatures cannot be targeted by the transform trigger")
    void noTransformChoiceWhenAllDefenderCreaturesHaveHexproof() {
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        addCreatureReady(player2, new DrogskolCaptain());
        addCreatureReady(player2, new DrogskolCaptain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(soulSeizer.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Removing Ghastly Haunting returns the enchanted creature to its original controller")
    void removingHauntingEndsControlEffect() {
        Permanent soulSeizer = addCreatureReady(player1, new SoulSeizer());
        soulSeizer.setAttacking(true);
        Permanent target = addCreatureReady(player2, new NiblisOfTheMist());

        resolveCombat();
        chooseTriggerTarget(target);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.getPermanentRemovalService().removePermanentToHand(gd, soulSeizer);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(soulSeizer.getOriginalCard()).isInstanceOf(SoulSeizer.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(soulSeizer.getOriginalCard());
    }

    private void chooseTriggerTarget(Permanent target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

}
