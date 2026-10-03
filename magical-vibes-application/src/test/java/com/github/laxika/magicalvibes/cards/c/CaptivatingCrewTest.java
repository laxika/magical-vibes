package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MesmericOrb;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptivatingCrew.class, RaptorCompanion.class, CobbledWings.class, MesmericOrb.class})
class CaptivatingCrewTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an opponent's creature")
    void activatingPutsOnStack() {
        addReadyCrew(player1);
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability gains control, untaps, and grants haste")
    void resolvesGainControlUntapAndHaste() {
        addReadyCrew(player1);
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        target.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        addReadyCrew(player1);
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        addReadyCrew(player1);
        Permanent ownCreature = addCreatureReady(player1, new RaptorCompanion());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyCrew(player1);
        addCreatureReady(player2, new RaptorCompanion()); // valid target so ability is activatable
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CobbledWings());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyCrew(player1);
        Permanent target = addCreatureReady(player2, new RaptorCompanion());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyCrew(player1);
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    void gainsControlBeforeUntappingForMesmericOrb() {
        addReadyCrew(player1);
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        target.tap();
        harness.setLibrary(player1, java.util.List.of(new RaptorCompanion(), new RaptorCompanion()));
        harness.setLibrary(player2, java.util.List.of(new RaptorCompanion(), new RaptorCompanion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        addReadyCrew(player1);
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsTurn() {
        addReadyCrew(player1);
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithAbilityAlreadyOnStack() {
        addReadyCrew(player1);
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        harness.addMana(player1, ManaColor.RED, 8);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new CaptivatingCrew());
        crew.setSummoningSick(true);
        crew.tap();
        Permanent target = addCreatureReady(player2, new RaptorCompanion());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyCrew(Player player) {
        return addCreatureReady(player, new CaptivatingCrew());
    }
}
