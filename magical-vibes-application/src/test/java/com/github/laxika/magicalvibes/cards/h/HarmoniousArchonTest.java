package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArchonOfJustice;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmoniousArchon.class, ArchonOfJustice.class, GrizzlyBears.class})
class HarmoniousArchonTest extends BaseCardTest {

    @Test
    @DisplayName("Non-Archon creatures on the battlefield have base 3/3")
    void setsNonArchonCreaturesToThreeThree() {
        addCreatureReady(player1, new HarmoniousArchon());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingArchon = addCreatureReady(player2, new ArchonOfJustice());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingArchon)).isNotEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingArchon)).isNotEqualTo(3);
    }

    @Test
    @DisplayName("Entering Harmonious Archon creates two Human tokens")
    void createsTwoHumanTokens() {
        castArchon();
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Human");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Human tokens retain counters when the Archon's base-setting effect ends")
    void tokensRetainCountersAfterArchonLeaves() {
        castArchon();
        resolveAllTriggers();
        Permanent archon = findPermanent(player1, "Harmonious Archon");
        List<Permanent> tokens = findPermanents(player1, "Human");
        assertThat(tokens).hasSize(2);
        Permanent token = tokens.getFirst();
        token.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);

        harness.getPermanentRemovalService().removePermanentToHand(gd, archon);

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, tokens.get(1))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tokens.get(1))).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter trigger creates 1/1 Humans even if the Archon leaves before resolution")
    void enterTriggerSurvivesArchonLeaving() {
        castArchon();
        harness.passBothPriorities();
        Permanent archon = findPermanent(player1, "Harmonious Archon");
        assertThat(findPermanents(player1, "Human")).isEmpty();

        harness.getPermanentRemovalService().removePermanentToHand(gd, archon);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human")).hasSize(2).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
        assertThat(findPermanents(player2, "Human")).isEmpty();
    }

    @Test
    @DisplayName("A second Archon keeps Humans at 3/3 after the first leaves")
    void anotherArchonKeepsBasePowerAndToughnessSet() {
        castArchon();
        resolveAllTriggers();
        Permanent firstArchon = findPermanent(player1, "Harmonious Archon");
        Permanent secondArchon = addCreatureReady(player2, new HarmoniousArchon());

        harness.getPermanentRemovalService().removePermanentToHand(gd, firstArchon);

        assertThat(findPermanents(player1, "Human")).hasSize(2).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        });

        harness.getPermanentRemovalService().removePermanentToHand(gd, secondArchon);

        assertThat(findPermanents(player1, "Human")).hasSize(2).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    private void castArchon() {
        harness.setHand(player1, List.of(new HarmoniousArchon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
    }
}
