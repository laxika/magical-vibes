package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrimgrinCorpseBornTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has EntersTappedEffect and DoesntUntapDuringUntapStepEffect as static effects")
    void hasStaticEffects() {
        GrimgrinCorpseBorn card = new GrimgrinCorpseBorn();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .hasSize(2)
                .anySatisfy(e -> assertThat(e).isInstanceOf(EntersTappedEffect.class))
                .anySatisfy(e -> assertThat(e).isInstanceOf(DoesntUntapDuringUntapStepEffect.class));
    }

    @Test
    @DisplayName("Has sacrifice-another-creature activated ability with untap and counter effects")
    void hasActivatedAbility() {
        GrimgrinCorpseBorn card = new GrimgrinCorpseBorn();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(3);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        SacrificeCreatureCost sacCost = (SacrificeCreatureCost) ability.getEffects().get(0);
        assertThat(sacCost.excludeSelf()).isTrue();
        assertThat(ability.getEffects().get(1)).isInstanceOf(UntapSelfEffect.class);
        assertThat(ability.getEffects().get(2)).isInstanceOf(PutCountersOnSourceEffect.class);
    }

    @Test
    @DisplayName("Has ON_ATTACK effects: destroy target permanent and put counter on source")
    void hasOnAttackEffects() {
        GrimgrinCorpseBorn card = new GrimgrinCorpseBorn();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK))
                .hasSize(2)
                .anySatisfy(e -> assertThat(e).isInstanceOf(DestroyTargetPermanentEffect.class))
                .anySatisfy(e -> assertThat(e).isInstanceOf(PutCountersOnSourceEffect.class));
    }

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Grimgrin does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent grimgrin = addGrimgrinReady(player1);
        grimgrin.tap();

        advanceToNextTurn(player2);

        assertThat(grimgrin.isTapped()).isTrue();
    }

    // ===== Activated ability: sacrifice another creature to untap and add counter =====

    @Test
    @DisplayName("Sacrificing another creature untaps Grimgrin and adds a +1/+1 counter")
    void sacrificeCreatureUntapsAndAddsCounter() {
        Permanent grimgrin = addGrimgrinReady(player1);
        grimgrin.tap();
        Permanent bears = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Grimgrin should be untapped
        assertThat(grimgrin.isTapped()).isFalse();

        // Grimgrin should have a +1/+1 counter
        assertThat(grimgrin.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice Grimgrin to its own ability (excludeSelf)")
    void cannotSacrificeItself() {
        addGrimgrinReady(player1);
        // No other creatures — Grimgrin is the only creature

        // Should not be able to activate since there is no other creature to sacrifice
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing multiple creatures accumulates +1/+1 counters")
    void multipleActivationsAccumulateCounters() {
        Permanent grimgrin = addGrimgrinReady(player1);
        grimgrin.tap();
        addReadyCreature(player1);
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        // First activation
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gd.playerBattlefields.get(player1.getId())
                .stream().filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().get().getId());
        harness.passBothPriorities();

        assertThat(grimgrin.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Tap again for second activation
        grimgrin.tap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(grimgrin.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability works while untapped too (no tap cost)")
    void abilityWorksWhileUntapped() {
        Permanent grimgrin = addGrimgrinReady(player1);
        // Grimgrin starts untapped
        assertThat(grimgrin.isTapped()).isFalse();
        addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should still get the counter even when already untapped
        assertThat(grimgrin.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Attack trigger: destroy target creature and add counter =====

    @Test
    @DisplayName("Attacking with Grimgrin queues targeted attack trigger for target selection")
    void attackTriggerQueuesForTargetSelection() {
        addGrimgrinReady(player1);
        addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving attack trigger destroys the chosen creature and adds a counter")
    void attackTriggerDestroysCreatureAndAddsCounter() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent grimgrin = addGrimgrinReady(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        // Choose the opponent's creature as target
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent's creature should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));

        // Grimgrin should have a +1/+1 counter
        assertThat(grimgrin.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack trigger cannot target own creatures")
    void attackTriggerCannotTargetOwnCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addGrimgrinReady(player1);
        Permanent ownCreature = addReadyCreature(player1);
        addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        // Choosing own creature should fail
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.handlePermanentChosen(player1, ownCreature.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addGrimgrinReady(Player player) {
        Permanent perm = new Permanent(new GrimgrinCorpseBorn());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
