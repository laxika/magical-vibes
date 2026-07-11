package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElvishHandservantTest extends BaseCardTest {

    private void giveGiantSpell(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new BlindSpotGiant()));
        harness.addMana(caster, ManaColor.RED, 5);
    }

    @Test
    @DisplayName("Casting a Giant spell offers the controller a may ability")
    void giantSpellTriggers() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        giveGiantSpell(player1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting puts a +1/+1 counter on Elvish Handservant")
    void acceptAddsCounter() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        Permanent handservant = gd.playerBattlefields.get(player1.getId()).getFirst();
        giveGiantSpell(player1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(handservant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, handservant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, handservant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining leaves Elvish Handservant without a counter")
    void declineLeavesNoCounter() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        Permanent handservant = gd.playerBattlefields.get(player1.getId()).getFirst();
        giveGiantSpell(player1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(handservant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a non-Giant spell does not trigger the ability")
    void nonGiantDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElvishHandservant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Any player casting a Giant spell triggers the controller's ability")
    void opponentGiantTriggersController() {
        harness.addToBattlefield(player1, new ElvishHandservant());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveGiantSpell(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
