package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
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

@CardUsed({EncaseInIce.class, GoblinAssailant.class, GrizzlyBears.class, FugitiveWizard.class})
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

    private void castEncaseInIce(Permanent target) {
        harness.setHand(player1, List.of(new EncaseInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
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
