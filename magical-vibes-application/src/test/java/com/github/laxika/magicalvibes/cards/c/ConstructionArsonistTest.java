package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConstructionArsonist.class, GrizzlyBears.class, Opt.class, WoollyThoctar.class})
class ConstructionArsonistTest extends BaseCardTest {

    @Test
    void incorporatesAnInstantOrSorceryThatDamagesEachOpponentWhenCast() {
        ConstructionArsonist arsonist = new ConstructionArsonist();
        Opt chosen = new Opt();
        harness.setHand(player1, List.of(arsonist, chosen));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void getsPlusOnePlusOneForEachColorOfMulticoloredSpellUntilEndOfTurn() {
        Permanent arsonist = harness.addToBattlefieldAndReturn(player1, new ConstructionArsonist());
        harness.setHand(player1, List.of(new WoollyThoctar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(arsonist.getEffectivePower()).isEqualTo(5);
        assertThat(arsonist.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(arsonist.getEffectivePower()).isEqualTo(2);
        assertThat(arsonist.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForMonocoloredSpell() {
        Permanent arsonist = harness.addToBattlefieldAndReturn(player1, new ConstructionArsonist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(arsonist.getEffectivePower()).isEqualTo(2);
        assertThat(arsonist.getEffectiveToughness()).isEqualTo(2);
    }
}
