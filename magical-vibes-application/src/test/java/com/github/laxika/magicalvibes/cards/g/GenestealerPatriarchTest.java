package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GenestealerPatriarch.class, GrizzlyBears.class, Shock.class})
class GenestealerPatriarchTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts an infection counter on a defending creature")
    void attackingPutsInfectionCounterOnDefendingCreature() {
        addCreatureReady(player1, new GenestealerPatriarch());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        assertThat(defendingCreature.getCounterCount(CounterType.INFECTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with an infection counter dying creates a Tyranid copy")
    void infectedCreatureDyingCreatesTyranidCopy() {
        addCreatureReady(player1, new GenestealerPatriarch());
        Permanent infectedCreature = addCreatureReady(player2, new GrizzlyBears());
        infectedCreature.setCounterCount(CounterType.INFECTION, 1);

        killWithShock(infectedCreature);
        resolveAllTriggers();

        Permanent copy = findPermanents(player1, "Grizzly Bears").getFirst();
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getCard().getSubtypes())
                .contains(CardSubtype.BEAR, CardSubtype.TYRANID);
    }

    @Test
    @DisplayName("A creature without an infection counter does not create a copy")
    void uninfectedCreatureDyingDoesNotCreateCopy() {
        addCreatureReady(player1, new GenestealerPatriarch());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        killWithShock(creature);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
