package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DiabolicVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiabolicVision.class, GrizzlyBears.class, ScatheZombies.class, ThroneOfBone.class})
class ThroneOfBoneTest extends BaseCardTest {

    // ===== Controller casts black spell =====

    @Test
    @DisplayName("Controller casts black spell, pays {1}, gains 1 life")
    void controllerCastsBlackSpellAndPays() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");

        // Trigger goes on the stack unconditionally
        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Throne of Bone"));

        // Resolving the trigger prompts the may-pay choice
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        // Accept and pay {1}
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Controller casts black spell, declines to pay, no life gain")
    void controllerCastsBlackSpellAndDeclines() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting without enough mana gains no life")
    void acceptWithoutManaNoLife() {
        harness.addToBattlefield(player1, new ThroneOfBone());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Multicolored black spell triggers Throne of Bone")
    void multicoloredBlackSpellTriggers() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DiabolicVision(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each Throne of Bone triggers independently")
    void multipleThronesTriggerIndependently() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    // ===== Opponent casts black spell =====

    @Test
    @DisplayName("Opponent casts black spell, controller pays {1}, gains 1 life")
    void opponentCastsBlackSpellControllerPays() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new ScatheZombies(), "{2}{B}");

        // Resolve the trigger (controller of Throne of Bone chooses)
        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    // ===== Non-black spell does NOT trigger =====

    @Test
    @DisplayName("Non-black spell does not trigger Throne of Bone")
    void nonBlackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThroneOfBone());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Throne of Bone"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Each Throne of Bone triggers independently for one black spell")
    void eachThroneTriggersIndependently() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Throne of Bone")))
                .hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Trigger resolves after Throne of Bone leaves the battlefield")
    void triggerResolvesAfterThroneOfBoneLeavesBattlefield() {
        var throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, throne));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Tapped Throne of Bone triggers and its generic payment accepts colored mana")
    void tappedThroneAcceptsColoredMana() {
        var throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfBone());
        throne.tap();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(throne.isTapped()).isTrue();
    }

    @Test
    @DisplayName("One black spell allows only one payment per Throne of Bone")
    void oneTriggerDoesNotRepeatPayment() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Declining one Throne's payment does not prevent paying for another")
    void independentPaymentChoices() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addToBattlefield(player1, new ThroneOfBone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A black creature entering without being cast does not trigger Throne of Bone")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThroneOfBone());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new ScatheZombies());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
