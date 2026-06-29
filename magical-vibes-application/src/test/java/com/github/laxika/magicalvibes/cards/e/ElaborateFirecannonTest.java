package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndUntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ElaborateFirecannonTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has DoesntUntapDuringUntapStepEffect as static effect")
    void hasDoesntUntapStaticEffect() {
        ElaborateFirecannon card = new ElaborateFirecannon();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(DoesntUntapDuringUntapStepEffect.class);
    }

    @Test
    @DisplayName("Has activated ability: {4}, tap, deals 2 damage to any target")
    void hasActivatedAbility() {
        ElaborateFirecannon card = new ElaborateFirecannon();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{4}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect dmgEffect =
                (DealDamageToAnyTargetEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(dmgEffect.damage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Has upkeep trigger: MayEffect wrapping DiscardCardAndUntapSelfEffect")
    void hasUpkeepTrigger() {
        ElaborateFirecannon card = new ElaborateFirecannon();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DiscardCardAndUntapSelfEffect.class);
    }

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped firecannon does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent perm = addFirecannonReady(player1);
        perm.tap();

        advanceToNextTurn(player2);
        // Decline the upkeep trigger
        harness.handleMayAbilityChosen(player1, false);

        assertThat(perm.isTapped()).isTrue();
    }

    // ===== Activated ability: {4}, {T}: deal 2 damage =====

    @Test
    @DisplayName("Activated ability deals 2 damage to target creature")
    void activatedAbilityDeals2DamageToCreature() {
        addFirecannonReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // 2/2 Grizzly Bears takes 2 damage → dies
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activated ability deals 2 damage to player")
    void activatedAbilityDeals2DamageToPlayer() {
        addFirecannonReady(player1);
        harness.setLife(player2, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Activated ability taps the firecannon")
    void activatedAbilityTapsFirecannon() {
        Permanent perm = addFirecannonReady(player1);
        harness.setLife(player2, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(perm.isTapped()).isTrue();
    }

    // ===== Upkeep trigger: discard to untap =====

    @Test
    @DisplayName("Accepting upkeep may, discarding a card, untaps the firecannon")
    void acceptMayDiscardUntapsFirecannon() {
        Permanent perm = addFirecannonReady(player1);
        perm.tap();

        advanceToNextTurn(player2);

        // Set hand AFTER advancing (advanceToNextTurn clears hands)
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        // The upkeep trigger is on the stack as a MayEffect — resolve it
        harness.handleMayAbilityChosen(player1, true);

        // Should now be awaiting discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        // Discard the card
        harness.handleCardChosen(player1, 0);

        // Card should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Firecannon should now be untapped
        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining upkeep may keeps firecannon tapped")
    void declineMayKeepsFirecannonTapped() {
        Permanent perm = addFirecannonReady(player1);
        perm.tap();

        advanceToNextTurn(player2);

        // Set hand AFTER advancing (advanceToNextTurn clears hands)
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.handleMayAbilityChosen(player1, false);

        // Firecannon should remain tapped
        assertThat(perm.isTapped()).isTrue();
        // Hand should still have the Grizzly Bears (no discard happened)
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Accepting upkeep may with empty hand does not untap")
    void acceptMayWithEmptyHandDoesNotUntap() {
        Permanent perm = addFirecannonReady(player1);
        perm.tap();

        harness.setHand(player1, List.of());

        advanceToNextTurn(player2);

        harness.handleMayAbilityChosen(player1, true);

        // No cards to discard → firecannon stays tapped
        assertThat(perm.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addFirecannonReady(Player player) {
        Permanent perm = new Permanent(new ElaborateFirecannon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
