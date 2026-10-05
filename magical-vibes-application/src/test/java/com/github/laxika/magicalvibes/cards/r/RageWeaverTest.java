package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lure;
import com.github.laxika.magicalvibes.cards.s.SteadfastGuard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RageWeaver.class, DrudgeSkeletons.class, GrizzlyBears.class, SteadfastGuard.class,
        FugitiveWizard.class, Lure.class})
@DisplayName("Rage Weaver")
class RageWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack with target")
    void activatingPutsOnStackWithTarget() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability grants haste to black creature")
    void resolvingGrantsHasteToBlackCreature() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Resolving ability grants haste to green creature")
    void resolvingGrantsHasteToGreenCreature() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's green creature")
    void canTargetOpponentGreenCreature() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is removed at end of turn")
    void hasteRemovedAtEndOfTurn() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target white creature")
    void cannotTargetWhiteCreature() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new SteadfastGuard());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Cannot target blue creature")
    void cannotTargetBlueCreature() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Cannot target a green noncreature permanent")
    void cannotTargetGreenNoncreaturePermanent() {
        addCreatureReady(player1, new RageWeaver());
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Lure());
        target.setAttachedTo(enchantedCreature.getId());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Rage Weaver can activate repeatedly")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new RageWeaver());
        weaver.setSummoningSick(true);
        weaver.setTapped(true);
        Permanent blackTarget = addCreatureReady(player1, new DrudgeSkeletons());
        Permanent greenTarget = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, blackTarget.getId());
        harness.activateAbility(player1, 0, null, greenTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blackTarget.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(greenTarget.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(weaver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability resolves after Rage Weaver leaves the battlefield")
    void abilityResolvesAfterSourceLeaves() {
        Permanent weaver = addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(weaver);
        gd.playerGraveyards.get(player1.getId()).add(weaver.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability does not grant haste if its target leaves before resolution")
    void abilityDoesNotResolveForRemovedTarget() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granted haste lets a newly entered green creature attack")
    void grantedHasteAllowsSummoningSickCreatureToAttack() {
        addCreatureReady(player1, new RageWeaver());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        declareAttackers(java.util.List.of(1));

        assertThat(target.isTapped()).isTrue();
    }
}
