package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.CardUsedExtension;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
@CardUsed({ChancellorOfTheDross.class, PorcelainLegionnaire.class})
@ExtendWith(CardUsedExtension.class)
class ChancellorOfTheDrossTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
    }

    @Test
    @DisplayName("Chancellor reveal is chosen before the first turn")
    void openingHandTriggerPromptsMayAbility() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.skipMulligan();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Revealed Chancellor creates a mandatory trigger at the first upkeep")
    void mayEffectGoesOnStackAtFirstUpkeep() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chancellor of the Dross");
    }

    @Test
    @DisplayName("Resolving Chancellor trigger causes opponent to lose 3 life and controller to gain 3 life")
    void openingHandTriggerDrainsLife() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Declining Chancellor reveal does not put anything on the stack")
    void decliningRevealDoesNotTrigger() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple Chancellors in hand each prompt separately")
    void multipleChancellorsInHandTriggerSeparately() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross(), new ChancellorOfTheDross()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Both players can trigger Chancellors from their opening hands")
    void bothPlayersCanTriggerChancellors() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.setHand(player2, List.of(new ChancellorOfTheDross()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Chancellor does not trigger from hand on subsequent turns")
    void doesNotTriggerOnSubsequentTurns() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting Chancellor of the Dross puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Chancellor of the Dross");
    }

    @Test
    @DisplayName("Chancellor stays in hand after opening hand trigger (it is not removed)")
    void chancellorRemainsInHandAfterTrigger() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chancellor of the Dross"));
    }

    @Test
    @DisplayName("Revealed Chancellor gains the full loss even if the opponent starts below three life")
    void drainCanTakeOpponentBelowZero() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.setLife(player2, 1);
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, -2);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Chancellor gains life from unblocked combat damage")
    void combatDamageGainsLife() {
        harness.skipMulligan();
        Permanent chancellor = harness.addToBattlefieldAndReturn(player1, new ChancellorOfTheDross());
        chancellor.setSummoningSick(false);
        chancellor.setAttacking(true);
        gd.activePlayerId = player1.getId();
        gd.currentStep = TurnStep.COMBAT_DAMAGE;

        harness.resolveCombatDamage();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("A ground creature cannot block Chancellor, but another Chancellor can")
    void flyingRestrictsBlockers() {
        harness.skipMulligan();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ChancellorOfTheDross());
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new PorcelainLegionnaire());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new ChancellorOfTheDross());
        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, groundBlocker, attacker, defenders))
                .isFalse();
        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, flyingBlocker, attacker, defenders))
                .isTrue();
    }
}
