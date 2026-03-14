package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DrossHopper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FurnaceCelebrationTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has MayPayManaEffect wrapping DealDamageToAnyTargetEffect on ON_ALLY_PERMANENT_SACRIFICED")
    void hasCorrectStructure() {
        FurnaceCelebration card = new FurnaceCelebration();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED).getFirst())
                .isInstanceOf(MayPayManaEffect.class);
        MayPayManaEffect mayPay = (MayPayManaEffect) card.getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED).getFirst();
        assertThat(mayPay.manaCost()).isEqualTo("{2}");
        assertThat(mayPay.wrapped()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect damage = (DealDamageToAnyTargetEffect) mayPay.wrapped();
        assertThat(damage.damage()).isEqualTo(2);
    }

    // ===== Sacrifice triggers may ability =====

    @Test
    @DisplayName("Sacrificing a creature triggers may ability prompt")
    void sacrificeTriggersMayPrompt() {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        Permanent hopper = addReadyCreature(player1, new DrossHopper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, bears.getId());

        // Resolve Dross Hopper ability (on top)
        harness.passBothPriorities();
        // Resolve Furnace Celebration MayPayManaEffect — shows may prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept, pay, target creature =====

    @Test
    @DisplayName("Accepting pays {2} and deals 2 damage to target creature")
    void acceptPaysDamageToCreature() {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        Permanent hopper = addReadyCreature(player1, new DrossHopper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, bears.getId());

        // Resolve Dross Hopper ability (on top)
        harness.passBothPriorities();
        // Resolve Furnace Celebration MayPayManaEffect — shows may prompt
        harness.passBothPriorities();

        // Accept the may ability — pays {2}, inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Should be prompting for target selection
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the creature target — damage resolves inline
        harness.handlePermanentChosen(player1, hawkId);

        // Suntail Hawk (1/1) should be destroyed by 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"));

        // Mana should have been spent
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Accept, pay, target player =====

    @Test
    @DisplayName("Accepting pays {2} and deals 2 damage to target player")
    void acceptPaysDamageToPlayer() {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        Permanent hopper = addReadyCreature(player1, new DrossHopper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 1, null, bears.getId());

        // Resolve Dross Hopper ability (on top)
        harness.passBothPriorities();
        // Resolve Furnace Celebration MayPayManaEffect — shows may prompt
        harness.passBothPriorities();

        // Accept — pays {2}, inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Choose opponent as target — damage resolves inline
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not deal damage or spend mana")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        Permanent hopper = addReadyCreature(player1, new DrossHopper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 1, null, bears.getId());

        // Resolve Dross Hopper ability (on top)
        harness.passBothPriorities();
        // Resolve Furnace Celebration MayPayManaEffect — shows may prompt
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on stack from Furnace Celebration
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Furnace Celebration"));

        // Mana not spent
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        // No damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    // ===== Cannot pay =====

    @Test
    @DisplayName("Accepting with insufficient mana treats as decline")
    void cannotPayTreatsAsDecline() {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        Permanent hopper = addReadyCreature(player1, new DrossHopper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        // No mana added — cannot pay {2}

        harness.activateAbility(player1, 1, null, bears.getId());

        // Resolve Dross Hopper ability (on top)
        harness.passBothPriorities();
        // Resolve Furnace Celebration MayPayManaEffect — shows may prompt
        harness.passBothPriorities();

        // May prompt fires
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Accept, but cannot pay — auto-treated as decline
        harness.handleMayAbilityChosen(player1, true);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Furnace Celebration"));
    }

    // ===== Does not trigger for opponent =====

    @Test
    @DisplayName("Opponent sacrificing a creature does not trigger your Furnace Celebration")
    void opponentSacrificeDoesNotTrigger() {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        Permanent hopper = addReadyCreature(player2, new DrossHopper());
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player2, 0, null, bears.getId());

        // No may ability should fire for player1
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
    }

    // ===== Helper methods =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
