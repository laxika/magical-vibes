package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gigadrowse.class, GhostWarden.class, IzzetSignet.class})
class GigadrowseTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target permanent")
    void tapsTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        castGigadrowse(target, List.of());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        castGigadrowse(target, List.of("{U}", "{U}"));

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Replicate copies may choose a new permanent target")
    void replicateCopyMayTargetAnotherPermanent() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        castGigadrowse(originalTarget, List.of("{U}"));

        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.isTapped()).isTrue();
        assertThat(copyTarget.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Replicate cannot target a player")
    void replicateCannotTargetPlayer() {
        harness.setHand(player1, List.of(new Gigadrowse()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    @DisplayName("Can tap an artifact controlled by the caster without replicating")
    void tapsOwnArtifactWithoutReplicating() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IzzetSignet());
        castGigadrowse(target, List.of());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Gigadrowse");
    }

    @Test
    @DisplayName("An already tapped permanent remains a legal target")
    void canTargetTappedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        target.tap();
        castGigadrowse(target, List.of());

        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Gigadrowse");
    }

    @Test
    @DisplayName("A replicate copy can retarget after the original target leaves")
    void copyCanRetargetAfterOriginalTargetLeaves() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        castGigadrowse(originalTarget, List.of("{U}"));
        gd.playerBattlefields.get(player2.getId()).remove(originalTarget);
        harness.setGraveyard(player2, List.of(originalTarget.getCard()));

        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(copyTarget.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Gigadrowse")).hasSize(1);
    }

    @Test
    @DisplayName("Each replicate payment requires an additional blue mana")
    void cannotReplicateWithoutEnoughMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        harness.setHand(player1, List.of(new Gigadrowse()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantWithRepeatedCosts(
                player1, 0, target.getId(), List.of("{U}")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Gigadrowse");
        assertThat(target.isTapped()).isFalse();
    }

    private void castGigadrowse(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Gigadrowse()));
        harness.addMana(player1, ManaColor.BLUE, 1 + replicatePayments.size());
        harness.castInstantWithRepeatedCosts(player1, 0, target.getId(), replicatePayments);
    }
}
