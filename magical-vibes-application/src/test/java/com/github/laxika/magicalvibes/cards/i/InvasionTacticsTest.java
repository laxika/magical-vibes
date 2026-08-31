package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KyoshiWarriors;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionTactics.class, GrizzlyBears.class, KyoshiWarriors.class})
class InvasionTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it gives your creatures +2/+2 until end of turn")
    void boostsOwnCreaturesOnEntry() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        cast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The entry boost wears off at end of turn")
    void boostWearsOff() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        cast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws one card when one or more Allies deal combat damage")
    void drawsOnceForMultipleAllies() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new InvasionTactics());
        Permanent firstAlly = addCreatureReady(player1, new KyoshiWarriors());
        Permanent secondAlly = addCreatureReady(player1, new KyoshiWarriors());
        firstAlly.setAttacking(true);
        secondAlly.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Non-Ally combat damage does not trigger the draw")
    void nonAllyDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new InvasionTactics());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void cast() {
        harness.setHand(player1, List.of(new InvasionTactics()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
    }
}
