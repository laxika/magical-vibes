package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.c.CrystallizedSerah;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerahFarron.class, CrystallizedSerah.class, AdelizTheCinderWind.class,
        ArvadTheCursed.class, CaptainSisay.class, GrizzlyBears.class})
class SerahFarronTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces only the first legendary creature spell each turn")
    void reducesOnlyFirstLegendaryCreatureSpellEachTurn() {
        addCreatureReady(player1, new SerahFarron());
        harness.setHand(player1, List.of(new AdelizTheCinderWind(), new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("A nonmatching spell does not use the reduction")
    void nonmatchingSpellDoesNotUseReduction() {
        addCreatureReady(player1, new SerahFarron());
        harness.setHand(player1, List.of(new GrizzlyBears(), new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("May transform at the beginning of combat with two other legendary creatures")
    void transformsWithTwoOtherLegendaryCreatures() {
        Permanent serah = addCreatureReady(player1, new SerahFarron());
        Permanent target = addCreatureReady(player1, new CaptainSisay());
        addCreatureReady(player1, new ArvadTheCursed());
        int powerBeforeTransform = gqs.getEffectivePower(gd, target);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(serah.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBeforeTransform + 2);
    }

    @Test
    @DisplayName("Does not offer transformation without two other legendary creatures")
    void doesNotTransformWithoutTwoOtherLegendaryCreatures() {
        Permanent serah = addCreatureReady(player1, new SerahFarron());
        addCreatureReady(player1, new ArvadTheCursed());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(serah.isTransformed()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
