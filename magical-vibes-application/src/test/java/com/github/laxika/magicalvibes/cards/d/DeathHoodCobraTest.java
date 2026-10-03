package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SpinedThopter;
import com.github.laxika.magicalvibes.cards.t.ThunderingTanadon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathHoodCobra.class, ThunderingTanadon.class, SpinedThopter.class})
class DeathHoodCobraTest extends BaseCardTest {

    @Test
    @DisplayName("Activating reach ability puts it on the stack")
    void activatingReachPutsOnStack() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(cobra.getId());
    }

    @Test
    @DisplayName("Resolving reach ability grants reach until end of turn")
    void resolvingReachAbilityGrantsReach() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Reach granted by ability resets at end of turn cleanup")
    void reachResetsAtEndOfTurn() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Activating deathtouch ability puts it on the stack")
    void activatingDeathtouchPutsOnStack() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(cobra.getId());
    }

    @Test
    @DisplayName("Resolving deathtouch ability grants deathtouch until end of turn")
    void resolvingDeathtouchAbilityGrantsDeathtouch() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch granted by ability resets at end of turn cleanup")
    void deathtouchResetsAtEndOfTurn() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Can activate both abilities to gain both reach and deathtouch")
    void canActivateBothAbilities() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Death-Hood Cobra")
    void activatingAbilityDoesNotTap() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cobra.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent cobra = addCobraReady(player1);
        cobra.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Death-Hood Cobra with deathtouch kills blocker regardless of toughness")
    void deathtouchKillsBlocker() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Activate deathtouch
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();

        // Set up combat with a large blocker
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ThunderingTanadon());

        cobra.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Blocker should be dead from deathtouch
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Ability resolves without granting reach to a replacement Cobra when its source leaves")
    void abilityDoesNotAffectReplacementCobra() {
        addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new DeathHoodCobra());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Both abilities can be activated while summoning sick and tapped")
    void bothAbilitiesWorkWhileSummoningSickAndTapped() {
        Permanent cobra = harness.addToBattlefieldAndReturn(player1, new DeathHoodCobra());
        cobra.setSummoningSick(true);
        cobra.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
        assertThat(cobra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Both keyword grants affect only the Cobra whose abilities were activated")
    void keywordGrantsAffectOnlyTheirSource() {
        Permanent cobra = addCobraReady(player1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DeathHoodCobra());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DeathHoodCobra());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Reach permits blocking a flying creature only after the ability resolves")
    void reachAllowsBlockingFlyingCreature() {
        Permanent cobra = addCobraReady(player1);
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new SpinedThopter());
        flyer.setAttacking(true);
        assertThat(bls.canBlockAttacker(gd, cobra, flyer,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(bls.canBlockAttacker(gd, cobra, flyer,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, cobra, flyer,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    private Permanent addCobraReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DeathHoodCobra());
        perm.setSummoningSick(false);
        return perm;
    }
}
