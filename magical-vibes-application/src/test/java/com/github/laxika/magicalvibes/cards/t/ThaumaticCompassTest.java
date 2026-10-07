package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RavenousDaggertooth;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThaumaticCompass.class, Forest.class, RavenousDaggertooth.class})
class ThaumaticCompassTest extends BaseCardTest {

    @Test
    @DisplayName("Search ability finds basic land and puts it in hand")
    void searchAbilityFindsBasicLand() {
        Card basicLand = new Forest();
        Card nonLand = new RavenousDaggertooth();
        harness.setLibrary(player1, List.of(nonLand, basicLand));

        Permanent compass = addArtifactReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int idx = indexOf(player1, compass);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities(); // resolve ability

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).containsExactly(basicLand);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).contains(basicLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonLand);
        assertThat(compass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Transforms at end step with exactly 7 lands")
    void transformsWithSevenLands() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 7; i++) {
            addLandReady(player1);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step, trigger goes on stack
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(compass.isTransformed()).isTrue();
        assertThat(compass.getCard().getName()).isEqualTo("Spires of Orazca");
    }

    @Test
    @DisplayName("Transforms at end step with more than 7 lands")
    void transformsWithEightLands() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 8; i++) {
            addLandReady(player1);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(compass.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at end step with only 6 lands")
    void doesNotTransformWithSixLands() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 6; i++) {
            addLandReady(player1);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(compass.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 7; i++) {
            addLandReady(player1);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(compass.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Spires of Orazca tap adds one colorless mana")
    void spiresBasicTapAddsColorless() {
        Permanent spires = addTransformedSpires(player1);

        int spiresIdx = indexOf(player1, spires);
        harness.activateAbility(player1, spiresIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Spires untaps and removes opponent's attacking creature from combat")
    void spiresUntapsAndRemovesAttacker() {
        Permanent spires = addTransformedSpires(player1);

        Card bear = new RavenousDaggertooth();
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        attacker.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int spiresIdx = indexOf(player1, spires);
        harness.activateAbility(player1, spiresIdx, 1, null, attacker.getId());
        harness.passBothPriorities(); // resolve ability

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Spires cannot target own attacking creature")
    void spiresCannotTargetOwnCreature() {
        Permanent spires = addTransformedSpires(player1);

        Card bear = new RavenousDaggertooth();
        Permanent ownAttacker = harness.addToBattlefieldAndReturn(player1, bear);
        ownAttacker.setSummoningSick(false);
        ownAttacker.setAttacking(true);
        ownAttacker.setAttackTarget(player2.getId());
        ownAttacker.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int spiresIdx = indexOf(player1, spires);
        assertThatThrownBy(() -> harness.activateAbility(player1, spiresIdx, 1, null, ownAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spires cannot target non-attacking creature")
    void spiresCannotTargetNonAttacker() {
        Permanent spires = addTransformedSpires(player1);

        Card bear = new RavenousDaggertooth();
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, bear);
        nonAttacker.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int spiresIdx = indexOf(player1, spires);
        assertThatThrownBy(() -> harness.activateAbility(player1, spiresIdx, 1, null, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTransformWhenLandCountFallsBeforeResolution() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 7; i++) {
            addLandReady(player1);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.passBothPriorities();
        assertThat(compass.isTransformed()).isFalse();
    }

    @Test
    void opponentsLandsDoNotCountTowardTransformation() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 6; i++) {
            addLandReady(player1);
        }
        addLandReady(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(compass.isTransformed()).isFalse();
    }

    @Test
    void spiresDoesNotUntapCreatureThatStoppedAttackingBeforeResolution() {
        Permanent spires = addTransformedSpires(player1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        attacker.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, indexOf(player1, spires), 1, null, attacker.getId());
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(spires.isTapped()).isTrue();
    }

    @Test
    void searchMayFailToFindEvenWithBasicLandAvailable() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of());
        Permanent compass = addArtifactReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, indexOf(player1, compass), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void transformationPreservesTappedStatus() {
        Permanent compass = addArtifactReady(player1);
        compass.tap();
        for (int i = 0; i < 7; i++) {
            addLandReady(player1);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(compass.isTransformed()).isTrue();
        assertThat(compass.isTapped()).isTrue();
    }

    private Permanent addArtifactReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ThaumaticCompass());
    }

    private Permanent addTransformedSpires(Player player) {
        Permanent perm = addArtifactReady(player);
        perm.setCard(perm.getCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private Permanent addLandReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
