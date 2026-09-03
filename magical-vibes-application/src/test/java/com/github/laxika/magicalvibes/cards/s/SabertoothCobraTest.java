package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.p.PsionicGift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.PoisonAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SabertoothCobra.class, GiantMantis.class, Incinerate.class})
class SabertoothCobraTest extends BaseCardTest {

    private int poison() {
        return gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);
    }

    /** Player1's Sabertooth Cobra deals its combat damage to player2. */
    private void dealCombatDamageToPlayer2() {
        resolveCombat();
        resolveAllTriggers();
    }

    /** Advance to player2's upkeep and resolve the delayed obligation into the pay-or-poison prompt. */
    private void advanceToPlayer2UpkeepObligation() {
        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Combat damage gives the damaged player a poison counter immediately")
    void damageGivesPoison() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        cobra.setAttacking(true);

        dealCombatDamageToPlayer2();

        assertThat(poison()).isEqualTo(1);
    }

    @Test
    @CardUsed(PsionicGift.class)
    @DisplayName("Both damage effects resolve as one triggered ability")
    void damageCreatesOneTriggeredAbility() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        Permanent gift = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        gift.setAttachedTo(cobra.getId());

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Declining to pay {2} gives another poison counter at the damaged player's next upkeep")
    void declineGivesSecondPoison() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        cobra.setAttacking(true);

        dealCombatDamageToPlayer2();
        advanceToPlayer2UpkeepObligation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(poison()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {2} at the next upkeep prompt avoids the second poison counter")
    void payAvoidsSecondPoisonAtUpkeepPrompt() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        cobra.setAttacking(true);

        dealCombatDamageToPlayer2();
        advanceToPlayer2UpkeepObligation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player2, ManaColor.WHITE, 2); // mana empties between steps — add it at payment time
        harness.handleMayAbilityChosen(player2, true);

        assertThat(poison()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @CardUsed(PsionicGift.class)
    @DisplayName("Noncombat damage also gives the damaged player a poison counter")
    void noncombatDamageGivesPoison() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        Permanent gift = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        gift.setAttachedTo(cobra.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(poison()).isEqualTo(1);
        assertThat(gd.getDelayedActions(PoisonAtNextUpkeepUnlessPays.class)).hasSize(1);
    }

    @Test
    @CardUsed(PsionicGift.class)
    @DisplayName("The delayed poison still applies after the Cobra leaves the battlefield")
    void delayedPoisonSurvivesSourceLeavingBattlefield() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        Permanent gift = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        gift.setAttachedTo(cobra.getId());

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player2, 0, cobra.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cobra);
        resolveAllTriggers();

        assertThat(poison()).isEqualTo(1);
        advanceToPlayer2UpkeepObligation();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(poison()).isEqualTo(2);
    }

    @Test
    @DisplayName("No poison and no upkeep obligation when the Cobra is blocked")
    void blockedCreatesNothing() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        cobra.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantMantis());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        dealCombatDamageToPlayer2();

        assertThat(poison()).isZero();
        assertThat(gd.getDelayedActions(PoisonAtNextUpkeepUnlessPays.class)).isEmpty();
    }
}
