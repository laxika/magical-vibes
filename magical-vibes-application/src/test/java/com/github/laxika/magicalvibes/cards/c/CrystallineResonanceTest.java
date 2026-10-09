package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrystallineResonance.class, Censor.class, GrizzlyBears.class, Clone.class})
class CrystallineResonanceTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling offers another permanent as a copy target")
    void cyclingQueuesAnotherPermanentTarget() {
        Permanent resonance = addResonance();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cycleCard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(resonance.getId());
    }

    @Test
    @DisplayName("Accepting the trigger copies a permanent until the controller's next turn")
    void copiesUntilControllersNextTurn() {
        Permanent resonance = addResonance();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveCopy(bears);

        assertThat(gqs.isCreature(gd, resonance)).isTrue();
        assertThat(gqs.getEffectivePower(gd, resonance)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, resonance)).isEqualTo(2);

        endTurn(player1);
        assertThat(gqs.isCreature(gd, resonance)).isTrue();

        endTurn(player2);
        assertThat(gqs.isCreature(gd, resonance)).isFalse();
    }

    @Test
    @DisplayName("The copied permanent retains Crystalline Resonance's cycling trigger")
    void copiedPermanentRetainsCyclingTrigger() {
        Permanent resonance = addResonance();
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveCopy(firstTarget);
        cycleCard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(firstTarget.getId(), secondTarget.getId())
                .doesNotContain(resonance.getId());
    }

    @Test
    @DisplayName("Declining the copy leaves the enchantment unchanged")
    void mayDeclineCopy() {
        Permanent resonance = addResonance();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cycleCard();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, resonance)).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A permanent controlled by an opponent can be copied")
    void copiesOpponentsPermanent() {
        Permanent resonance = addResonance();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCopy(bears);

        assertThat(gqs.isCreature(gd, resonance)).isTrue();
        assertThat(gqs.getEffectivePower(gd, resonance)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(resonance);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("A second copy replaces the first and all copies expire next turn")
    void secondCopyReplacesFirst() {
        Permanent resonance = addResonance();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CrystallineResonance());

        resolveCopy(bears);
        assertThat(gqs.isCreature(gd, resonance)).isTrue();
        resolveCopy(enchantment);
        assertThat(gqs.isCreature(gd, resonance)).isFalse();

        endTurn(player1);
        endTurn(player2);
        assertThat(gqs.isCreature(gd, resonance)).isFalse();
        cycleCard();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent cycling a card does not trigger the enchantment")
    void opponentsCyclingDoesNotTrigger() {
        Permanent resonance = addResonance();
        harness.addToBattlefield(player1, new GrizzlyBears());

        cycleCard(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.isCreature(gd, resonance)).isFalse();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Clone of Resonance retains its inherited trigger after copying again")
    void cloneRetainsInheritedCyclingTrigger() {
        Permanent resonance = addResonance();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveCopy(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, resonance.getId());
        Permanent clone = gd.playerBattlefields.get(player2.getId()).getFirst();

        cycleCard(player2);
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        cycleCard(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId(), resonance.getId())
                .doesNotContain(clone.getId());
    }

    @Test
    @DisplayName("Copying a face-down permanent copies its face-down characteristics")
    void copiesFaceDownCharacteristics() {
        Permanent resonance = addResonance();
        Permanent manifestedClone = harness.addToBattlefieldAndReturn(player2, new Clone());
        manifestedClone.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        resolveCopy(manifestedClone);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(resonance);
        assertThat(gqs.getEffectivePower(gd, resonance)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, resonance)).isEqualTo(2);
        assertThat(resonance.isFaceDown()).isFalse();
    }

    private Permanent addResonance() {
        return harness.addToBattlefieldAndReturn(player1, new CrystallineResonance());
    }

    private void cycleCard() {
        cycleCard(player1);
    }

    private void cycleCard(Player player) {
        harness.setHand(player, List.of(new Censor()));
        harness.setLibrary(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.activateHandAbility(player, 0, null);
        harness.passBothPriorities();
    }

    private void resolveCopy(Permanent target) {
        cycleCard();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Player nextPlayer = activePlayer == player1 ? player2 : player1;
        harness.passUntilWithNoAttackers(nextPlayer, TurnStep.UPKEEP);
    }
}
