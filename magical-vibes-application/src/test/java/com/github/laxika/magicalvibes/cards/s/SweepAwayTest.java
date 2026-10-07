package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.cards.b.BlindingDrone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SweepAway.class, BlindingDrone.class, Wastes.class})
class SweepAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a non-attacking creature to its owner's hand")
    void returnsNonAttackingCreatureToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindingDrone());

        prepareSweepAway();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Blinding Drone");
        harness.assertInHand(player2, "Blinding Drone");
    }

    @Test
    @DisplayName("Puts an attacking creature on top of its owner's library")
    void putsAttackingCreatureOnTopOfLibrary() {
        Permanent target = addAttacker();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        prepareSweepAway();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Blinding Drone");
        harness.assertNotInHand(player2, "Blinding Drone");
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(deckBefore + 1);
        assertThat(library.getFirst().getName()).isEqualTo("Blinding Drone");
    }

    @Test
    @DisplayName("Returns the creature to hand if it stops attacking before resolution")
    void returnsToHandIfTargetStopsAttacking() {
        Permanent target = addAttacker();

        prepareSweepAway();
        harness.castInstant(player1, 0, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Blinding Drone");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Wastes());

        harness.setHand(player1, List.of(new SweepAway()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster may return an attacking creature to hand instead of the library")
    void mayDeclineLibraryDestination() {
        Permanent target = addAttacker();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        prepareSweepAway();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Blinding Drone");
        harness.assertInHand(player2, "Blinding Drone");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Can return the caster's own creature to hand")
    void returnsOwnCreatureToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BlindingDrone());

        prepareSweepAway();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Blinding Drone");
        harness.assertInHand(player1, "Blinding Drone");
        harness.assertNotInHand(player2, "Blinding Drone");
    }

    @Test
    @DisplayName("Does not affect a target that leaves the battlefield before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = addAttacker();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        prepareSweepAway();
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Blinding Drone");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    private void prepareSweepAway() {
        harness.setHand(player1, List.of(new SweepAway()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new BlindingDrone());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
