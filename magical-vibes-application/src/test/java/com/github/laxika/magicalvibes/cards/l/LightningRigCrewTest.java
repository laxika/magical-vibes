package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightningRigCrewTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has tap activated ability dealing 1 damage to each opponent")
    void hasActivatedAbility() {
        LightningRigCrew card = new LightningRigCrew();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(DealDamageToEachOpponentEffect.class);
        DealDamageToEachOpponentEffect dmgEffect =
                (DealDamageToEachOpponentEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(dmgEffect.damage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has Pirate spell-cast trigger with untap self")
    void hasPirateTrigger() {
        LightningRigCrew card = new LightningRigCrew();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardSubtypePredicate.class);
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(UntapSelfEffect.class);
    }

    // ===== Activated ability: {T}: deal 1 damage to each opponent =====

    @Test
    @DisplayName("Tap ability deals 1 damage to each opponent")
    void tapAbilityDealsDamage() {
        addCrewReady(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tap ability taps the creature")
    void tapAbilityTapsCreature() {
        Permanent perm = addCrewReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(perm.isTapped()).isTrue();
    }

    // ===== Pirate spell trigger untaps =====

    @Test
    @DisplayName("Casting a Pirate spell triggers untap")
    void pirateSpellTriggersUntap() {
        addCrewReady(player1);
        harness.setHand(player1, List.of(new DireFleetCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Dire Fleet Captain on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Lightning-Rig Crew"));
    }

    @Test
    @DisplayName("Resolving Pirate spell trigger untaps the crew")
    void pirateTriggerUntapsCrew() {
        Permanent perm = addCrewReady(player1);
        perm.tap();
        assertThat(perm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new DireFleetCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        // Resolve the triggered ability (LIFO — trigger on top)
        harness.passBothPriorities();

        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Pirate creature does not trigger untap")
    void nonPirateDoesNotTrigger() {
        addCrewReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting a Pirate spell does not trigger controller's crew")
    void opponentPirateDoesNotTrigger() {
        addCrewReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DireFleetCaptain()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Tap + untap interaction =====

    @Test
    @DisplayName("Can tap for damage, then cast Pirate to untap and tap again")
    void tapUntapTapAgain() {
        Permanent perm = addCrewReady(player1);
        harness.setLife(player2, 20);

        // First activation: tap to deal 1 damage
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(perm.isTapped()).isTrue();

        // Cast a Pirate spell to untap
        harness.setHand(player1, List.of(new DireFleetCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        // Resolve the triggered untap ability
        harness.passBothPriorities();
        assertThat(perm.isTapped()).isFalse();

        // Resolve the Pirate creature spell
        harness.passBothPriorities();

        // Second activation: tap again to deal 1 more damage
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    // ===== Helpers =====

    private Permanent addCrewReady(Player player) {
        Permanent perm = new Permanent(new LightningRigCrew());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
