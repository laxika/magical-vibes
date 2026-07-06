package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdditiveEvolutionTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    

    @Test
    @DisplayName("ETB creates a 3/3 Fractal token with three +1/+1 counters")
    void etbCreatesFractalTokenWithCounters() {
        harness.setHand(player1, List.of(new AdditiveEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent fractal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Fractal".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, fractal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fractal)).isEqualTo(3);
    }

    @Test
    @DisplayName("Beginning of combat puts a counter on target creature you control and grants vigilance")
    void beginningOfCombatBuffsTargetCreature() {
        harness.addToBattlefield(player1, new AdditiveEvolution());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst()
                .orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target opponent's creature at beginning of combat")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new AdditiveEvolution());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(opponentBearsId);
    }

    @Test
    @DisplayName("Does not trigger during opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new AdditiveEvolution());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AdditiveEvolution());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Casting puts enchantment spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AdditiveEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Additive Evolution");
    }
}
