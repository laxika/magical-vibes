package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Briarhorn.class, WoodlandChangeling.class})
class BriarhornTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: ETB gives target creature +3/+3 and Briarhorn stays on the battlefield")
    void hardcastBoostsTargetAndStays() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Briarhorn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Woodland Changeling");
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);

        harness.assertOnBattlefield(player1, "Briarhorn");
        harness.assertNotInGraveyard(player1, "Briarhorn");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Briarhorn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Woodland Changeling");
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Woodland Changeling");
        assertThat(bears.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Evoke: paying only {1}{G}, ETB still gives +3/+3 and Briarhorn is sacrificed")
    void evokeBoostsTargetAndSacrifices() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Briarhorn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Woodland Changeling");
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);

        harness.assertNotOnBattlefield(player1, "Briarhorn");
        harness.assertInGraveyard(player1, "Briarhorn");
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Briarhorn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature â€” ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB â€” fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
    @Test
    @DisplayName("Briarhorn can enter an empty battlefield and target itself")
    void canTargetItselfAfterEntering() {
        harness.setHand(player1, List.of(new Briarhorn()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Briarhorn"));
        resolveAllTriggers();

        Permanent briarhorn = findPermanent(player1, "Briarhorn");
        assertThat(briarhorn.getEffectivePower()).isEqualTo(6);
        assertThat(briarhorn.getEffectiveToughness()).isEqualTo(6);
        harness.assertNotInGraveyard(player1, "Briarhorn");
    }

    @Test
    @DisplayName("Evoke sacrifice still resolves when the boost target becomes illegal")
    void evokeSacrificesEvenIfBoostTargetRemoved() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Briarhorn()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreatureWithEvoke(player1, 0,
                harness.getPermanentId(player2, "Woodland Changeling"));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Briarhorn");
        harness.assertInGraveyard(player1, "Briarhorn");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Flash allows casting Briarhorn during the opponent's upkeep")
    void flashAllowsCastingDuringOpponentsUpkeep() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Briarhorn()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0,
                harness.getPermanentId(player2, "Woodland Changeling"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Briarhorn");
        Permanent target = findPermanent(player2, "Woodland Changeling");
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Controller chooses the order of Briarhorn's boost and evoke sacrifice triggers")
    void controllerChoosesEvokeTriggerOrder() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Briarhorn()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreatureWithEvoke(player1, 0,
                harness.getPermanentId(player2, "Woodland Changeling"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Briarhorn");
        var order = gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        assertThat(gd.stack).hasSize(2);
        harness.handleListChoice(player1, order.options().getLast());
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Woodland Changeling").getEffectivePower()).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Briarhorn");
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Briarhorn");
    }
    @Override
    protected void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            var choice = gd.interaction.activeInteraction(
                    com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice.class);
            if (choice != null && choice.context()
                    instanceof com.github.laxika.magicalvibes.model.ChoiceContext.SpellCastTriggerOrder) {
                harness.handleListChoice(player1, choice.options().getFirst());
            } else if (gd.interaction.isAwaitingInput()) {
                break;
            } else {
                harness.passBothPriorities();
            }
        }
    }

}
