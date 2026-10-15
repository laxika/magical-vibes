package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.d.DivinersWand;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.w.WarrenWeirding;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GracefulReprieve.class, Disperse.class, DivinersWand.class, IndomitableAncients.class,
        WarrenWeirding.class})
class GracefulReprieveTest extends BaseCardTest {

    @Test
    @DisplayName("The delayed return is controlled by the player who cast Graceful Reprieve")
    void delayedTriggerBelongsToSpellController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IndomitableAncients());
        harness.setHand(player1, List.of(new GracefulReprieve()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        creature.setMarkedDamage(10);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Indomitable Ancients");
        harness.assertNotOnBattlefield(player1, "Indomitable Ancients");
    }

    @Test
    @DisplayName("The delayed return has Graceful Reprieve as its source")
    void delayedTriggerHasSpellAsSource() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IndomitableAncients());
        GracefulReprieve reprieve = new GracefulReprieve();
        harness.setHand(player1, List.of(reprieve));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        creature.setMarkedDamage(10);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(reprieve.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Indomitable Ancients");
    }

    @Test
    @DisplayName("A returned creature is a new object and does not return from a second death")
    void doesNotReturnAfterSecondDeath() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IndomitableAncients());
        harness.setHand(player1, List.of(new GracefulReprieve()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        creature.setMarkedDamage(10);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Indomitable Ancients");
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Indomitable Ancients");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        returned.setMarkedDamage(10);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Indomitable Ancients");
        harness.assertInGraveyard(player1, "Indomitable Ancients");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns the targeted creature to the battlefield under its owner's control when it dies this turn")
    void returnsCreatureWhenItDiesThisTurn() {
        harness.addToBattlefield(player2, new IndomitableAncients());
        harness.setHand(player1, List.of(new GracefulReprieve(), new WarrenWeirding()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Indomitable Ancients");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Kill the targeted creature later the same turn
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        // The card returns to the battlefield under player2's (owner's) control
        harness.assertOnBattlefield(player2, "Indomitable Ancients");
        harness.assertNotInGraveyard(player2, "Indomitable Ancients");
    }

    @Test
    @DisplayName("Does not return the creature if it survives the turn")
    void doesNotReturnWhenCreatureSurvives() {
        harness.addToBattlefield(player2, new IndomitableAncients());
        harness.setHand(player1, List.of(new GracefulReprieve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Indomitable Ancients");
        harness.castAndResolveInstant(player1, 0, targetId);

        // No death, so exactly one Indomitable Ancients remains and nothing entered the graveyard
        assertThat(findPermanents(player2, "Indomitable Ancients"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles and registers nothing when the target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new IndomitableAncients());
        harness.setHand(player1, List.of(new GracefulReprieve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Indomitable Ancients");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new DivinersWand());
        harness.setHand(player1, List.of(new GracefulReprieve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID divinersWandId = harness.getPermanentId(player2, "Diviner's Wand");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, divinersWandId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return a new object if the targeted creature leaves and returns before dying")
    void doesNotReturnNewObjectAfterTargetLeavesAndReturns() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new IndomitableAncients());
        harness.setHand(player1, List.of(new GracefulReprieve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Indomitable Ancients");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Indomitable Ancients");
        harness.assertInGraveyard(player2, "Indomitable Ancients");
    }

    @Test
    @DisplayName("Does not return the creature if it dies after the turn ends")
    void doesNotReturnWhenCreatureDiesAfterTurnEnds() {
        harness.addToBattlefield(player2, new IndomitableAncients());
        harness.setHand(player1, List.of(new GracefulReprieve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Indomitable Ancients");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Indomitable Ancients");
        harness.assertInGraveyard(player2, "Indomitable Ancients");
    }
}
