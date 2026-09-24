package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.i.IndebtedSamurai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KentaroTheSmilingCat.class, IndebtedSamurai.class, GoblinCohort.class})
class KentaroTheSmilingCatTest extends BaseCardTest {

    @Test
    @DisplayName("A Samurai spell can be cast for generic mana equal to its mana value")
    void samuraiSpellCastForGenericManaValue() {
        harness.addToBattlefield(player1, new KentaroTheSmilingCat());
        // Indebted Samurai costs {3}{W} (mana value 4) — with Kentaro it can be cast for {4}
        harness.setHand(player1, List.of(new IndebtedSamurai()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Indebted Samurai");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The alternative cost is not free — less generic mana than the mana value is not enough")
    void samuraiSpellNeedsFullManaValue() {
        harness.addToBattlefield(player1, new KentaroTheSmilingCat());
        harness.setHand(player1, List.of(new IndebtedSamurai()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A non-Samurai spell gets no alternative cost")
    void nonSamuraiSpellUnaffected() {
        harness.addToBattlefield(player1, new KentaroTheSmilingCat());
        // Goblin Cohort costs {R} — colorless mana cannot pay its red cost
        harness.setHand(player1, List.of(new GoblinCohort()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without Kentaro, a Samurai spell cannot be cast for generic mana alone")
    void noAlternativeCostWithoutKentaro() {
        harness.setHand(player1, List.of(new IndebtedSamurai()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Samurai spells are not affected by your Kentaro")
    void opponentSamuraiSpellsUnaffected() {
        harness.addToBattlefield(player1, new KentaroTheSmilingCat());
        harness.setHand(player2, List.of(new IndebtedSamurai()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When Kentaro becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent kentaro = addCreatureReady(player1, new KentaroTheSmilingCat());
        kentaro.setAttacking(true);
        addCreatureReady(player2, new GoblinCohort());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kentaro)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kentaro)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Kentaro blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new GoblinCohort());
        attacker.setAttacking(true);
        Permanent kentaro = addCreatureReady(player2, new KentaroTheSmilingCat());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kentaro)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kentaro)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Kentaro is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent kentaro = addCreatureReady(player1, new KentaroTheSmilingCat());
        kentaro.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gqs.getEffectivePower(gd, kentaro)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kentaro)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kentaro's Bushido bonus wears off at end of turn")
    void bushidoWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GoblinCohort());
        attacker.setAttacking(true);
        Permanent kentaro = addCreatureReady(player2, new KentaroTheSmilingCat());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kentaro)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kentaro)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kentaro)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kentaro)).isEqualTo(1);
    }
}
