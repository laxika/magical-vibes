package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvengersAssemble.class, AgentMariaHill.class, GrizzlyBears.class})
class AvengersAssembleTest extends BaseCardTest {

    @Test
    void boostsHeroesYouControlButNotOtherCreatures() {
        harness.addToBattlefield(player1, new AvengersAssemble());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());
        Permanent ownNonHero = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentHero = addCreatureReady(player2, new AgentMariaHill());

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonHero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentHero)).isEqualTo(1);
    }

    @Test
    void drawsAtEndStepWhenHeroEntersUnderYourControl() {
        harness.addToBattlefield(player1, new AvengersAssemble());
        harness.enterBattlefieldAndReturn(player1, new AgentMariaHill());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void drawsAtEndStepWhenYouAttackWithAHero() {
        harness.addToBattlefield(player1, new AvengersAssemble());
        addCreatureReady(player1, new AgentMariaHill());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(1));
        resolveEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void doesNotDrawWhenNeitherConditionWasMet() {
        harness.addToBattlefield(player1, new AvengersAssemble());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
