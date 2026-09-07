package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CharmedSleep.class, Forest.class, GrizzlyBears.class})
class CharmedSleepTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Charmed Sleep taps the enchanted creature")
    void enteringAuraTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CharmedSleep()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        enchantedCreature.tap();
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        otherCreature.tap();

        Permanent aura = new Permanent(new CharmedSleep());
        aura.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Charmed Sleep cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CharmedSleep()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
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
