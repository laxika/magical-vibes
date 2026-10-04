package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EncaseInIce.class, GoblinAssailant.class, GrizzlyBears.class, FugitiveWizard.class, Naturalize.class})
class EncaseInIceTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a red creature and taps it when it enters")
    void enchantsAndTapsRedCreature() {
        Permanent creature = addCreatureReady(player2, new GoblinAssailant());

        castEncaseInIce(creature);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can enchant a green creature and taps it when it enters")
    void enchantsAndTapsGreenCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castEncaseInIce(creature);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a creature that is neither red nor green")
    void cannotEnchantOffColorCreature() {
        Permanent legalTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent illegalTarget = addCreatureReady(player2, new FugitiveWizard());

        harness.setHand(player1, List.of(new EncaseInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, illegalTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a red or green creature");
        assertThat(legalTarget.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castEncaseInIce(creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps after Encase in Ice is removed")
    void creatureUntapsAfterAuraIsRemoved() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castEncaseInIce(creature);
        Permanent aura = findPermanent(player1, "Encase in Ice");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new EncaseInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Encase in Ice").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Tapping happens when the enter trigger resolves, not when the Aura enters")
    void tapWaitsForEnterTrigger() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EncaseInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Encase in Ice").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        resolveAllTriggers();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Destroying the Aura in response to its enter trigger does not stop the tap")
    void enterTriggerTapsAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EncaseInIce()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Encase in Ice");
        assertThat(creature.isTapped()).isFalse();
        harness.passPriority(player1);

        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Encase in Ice");
        assertThat(creature.isTapped()).isFalse();
        resolveAllTriggers();
        assertThat(creature.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can enchant your own creature without preventing other creatures from untapping")
    void locksOnlyEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.tap();

        castEncaseInIce(enchanted);
        harness.performUntapStep(player1);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An Aura whose target leaves before resolution does not enter or tap another creature")
    void targetLeavesBeforeAuraResolves() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EncaseInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Encase in Ice");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof EncaseInIce);
        assertThat(other.isTapped()).isFalse();
    }

    private void castEncaseInIce(Permanent target) {
        harness.setHand(player1, List.of(new EncaseInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
