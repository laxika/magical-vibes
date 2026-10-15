package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilburlindSnapper.class, DualShot.class, QuilledWolf.class})
class SilburlindSnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when no noncreature spell was cast this turn")
    void cannotAttackWithoutNoncreatureSpell() {
        addCreatureReady(player1, new SilburlindSnapper());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack after casting a noncreature spell this turn")
    void canAttackAfterCastingNoncreatureSpell() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SilburlindSnapper());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Casting a creature spell does not lift the attack restriction")
    void creatureSpellDoesNotLiftRestriction() {
        addCreatureReady(player1, new SilburlindSnapper());
        harness.setHand(player1, List.of(new QuilledWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not lift the restriction")
    void opponentsSpellDoesNotLiftRestriction() {
        addCreatureReady(player1, new SilburlindSnapper());
        harness.setHand(player2, List.of(new DualShot()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, List.<UUID>of());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature spell cast before the Snapper entered still counts")
    void spellCastBeforeEnteringBattlefieldCounts() {
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, List.<UUID>of());
        addCreatureReady(player1, new SilburlindSnapper());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("A noncreature spell from the previous turn does not count")
    void previousTurnsSpellDoesNotCount() {
        addCreatureReady(player2, new SilburlindSnapper());
        harness.setHand(player2, List.of(new DualShot()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, List.<UUID>of());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block without casting a noncreature spell")
    void canBlockWithoutNoncreatureSpell() {
        addCreatureReady(player1, new QuilledWolf());
        addCreatureReady(player2, new SilburlindSnapper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Quilled Wolf");
        harness.assertOnBattlefield(player2, "Silburlind Snapper");
        harness.assertLife(player2, 20);
    }
}
