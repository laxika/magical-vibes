package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AthreosShroudVeiled.class, GrizzlyBears.class, WalkingCorpse.class})
class AthreosShroudVeiledTest extends BaseCardTest {

    @Test
    void isNotCreatureBelowDevotionThreshold() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(4);

        assertThat(gqs.isCreature(gd, athreos)).isFalse();
    }

    @Test
    void becomesCreatureAtDevotionThreshold() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(5);

        assertThat(gqs.isCreature(gd, athreos)).isTrue();
    }

    @Test
    void putsACoinCounterOnAnotherTargetCreatureAtEndStep() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.COIN)).isEqualTo(1);
    }

    @Test
    void returnsCounteredCreatureFromGraveyardUnderItsControllersControl() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void returnsCounteredCreatureFromExileUnderItsControllersControl() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    @Test
    void doesNotReturnCreatureWithoutACoinCounter() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }
}
