package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DeeprootWarrior;
import com.github.laxika.magicalvibes.cards.v.VraskaRelicSeeker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElaborateFirecannon.class, DeeprootWarrior.class, VraskaRelicSeeker.class})
class ElaborateFirecannonTest extends BaseCardTest {

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

    @Test
    @DisplayName("Activated ability deals 2 damage to target creature")
    void activatedAbilityDeals2DamageToCreature() {
        addFirecannonReady(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DeeprootWarrior()).getId();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Lethal damage destroys the creature.
        harness.assertInGraveyard(player2, "Deeproot Warrior");
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

    @Test
    @DisplayName("Accepting upkeep may, discarding a card, untaps the firecannon")
    void acceptMayDiscardUntapsFirecannon() {
        Permanent perm = addFirecannonReady(player1);
        perm.tap();

        advanceToNextTurn(player2);

        // Set hand AFTER advancing (advanceToNextTurn clears hands)
        harness.setHand(player1, List.of(new DeeprootWarrior()));

        // Resolve the optional discard.
        harness.handleMayAbilityChosen(player1, true);

        // Should now be awaiting discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        // Discard the card
        harness.handleCardChosen(player1, 0);

        // Card should be in graveyard
        harness.assertInGraveyard(player1, "Deeproot Warrior");

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
        harness.setHand(player1, List.of(new DeeprootWarrior()));

        harness.handleMayAbilityChosen(player1, false);

        // Firecannon should remain tapped
        assertThat(perm.isTapped()).isTrue();
        // Hand should still have the Deeproot Warrior (no discard happened)
        harness.assertInHand(player1, "Deeproot Warrior");
    }

    @Test
    @DisplayName("Accepting upkeep may with empty hand does not untap")
    void acceptMayWithEmptyHandDoesNotUntap() {
        Permanent perm = addFirecannonReady(player1);
        perm.tap();

        harness.setHand(player1, List.of());

        advanceToNextTurn(player2);

        harness.handleMayAbilityChosen(player1, true);

        // Without a discard, the firecannon stays tapped.
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with only three mana")
    void cannotActivateWithInsufficientMana() {
        Permanent perm = addFirecannonReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(perm.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent perm = addFirecannonReady(player1);
        perm.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller")
    void canDealDamageToController() {
        addFirecannonReady(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Can discard during upkeep even when already untapped")
    void canDiscardWhileAlreadyUntapped() {
        Permanent perm = addFirecannonReady(player1);
        advanceToNextTurn(player2);
        harness.setHand(player1, List.of(new DeeprootWarrior()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Deeproot Warrior");
        harness.assertNotInHand(player1, "Deeproot Warrior");
        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent perm = addFirecannonReady(player1);
        perm.tap();
        harness.setHand(player1, List.of(new DeeprootWarrior()));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(perm.isTapped()).isTrue();
        harness.assertInHand(player1, "Deeproot Warrior");
    }

    @Test
    @DisplayName("Activated ability removes two loyalty from a planeswalker")
    void activatedAbilityDeals2DamageToPlaneswalker() {
        addFirecannonReady(player1);
        Permanent vraska = harness.enterBattlefieldAndReturn(player2, new VraskaRelicSeeker());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, vraska.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Vraska, Relic Seeker");
    }

    private Permanent addFirecannonReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ElaborateFirecannon());
        perm.setSummoningSick(false);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
    }
}
