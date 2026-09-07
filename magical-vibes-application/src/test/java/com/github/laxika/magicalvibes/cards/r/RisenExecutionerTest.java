package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RisenExecutioner.class, Gravedigger.class, GrizzlyBears.class})
class RisenExecutionerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Zombies you control get +1/+1")
    void boostsOtherZombiesButNotItself() {
        Permanent executioner = addCreatureReady(player1, new RisenExecutioner());
        Permanent zombie = addCreatureReady(player1, new Gravedigger());

        assertThat(gqs.getEffectivePower(gd, executioner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, executioner)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost an opponent's Zombie")
    void doesNotBoostOpponentsZombie() {
        Permanent zombie = addCreatureReady(player2, new Gravedigger());
        addCreatureReady(player1, new RisenExecutioner());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent executioner = new Permanent(new RisenExecutioner());
        executioner.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(executioner);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Can cast from the graveyard for its base cost with no other creature cards there")
    void canCastFromGraveyardForBaseCost() {
        harness.setGraveyard(player1, List.of(new RisenExecutioner()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs one more for each other creature card in the graveyard")
    void graveyardCastCostsOneMorePerOtherCreatureCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new RisenExecutioner()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castFromGraveyard(player1, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The graveyard surcharge is not applied to a hand cast")
    void graveyardSurchargeDoesNotApplyToHandCast() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RisenExecutioner()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
