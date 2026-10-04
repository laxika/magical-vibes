package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GriffinAerie.class, Revitalize.class})
class GriffinAerieTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/2 white flying Griffin token after gaining 3 life")
    void createsGriffinTokenAtThreeLifeGained() {
        harness.addToBattlefield(player1, new GriffinAerie());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        var griffins = findPermanents(player1, "Griffin");
        assertThat(griffins).hasSize(1);
        assertThat(griffins).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Does not create a token below the life-gain threshold")
    void noTokenBelowThreshold() {
        harness.addToBattlefield(player1, new GriffinAerie());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Griffin")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentEndStep() {
        harness.addToBattlefield(player1, new GriffinAerie());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player2);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Griffin")).isZero();
    }

    @Test
    @DisplayName("Life gained before Aerie enters counts even if that life is later lost")
    void earlierLifeGainCountsDespiteSubsequentLoss() {
        resolveRevitalize(player1);
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new GriffinAerie());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Griffin")).isEqualTo(1);
    }

    @Test
    @DisplayName("Gaining six life creates only one token per Aerie")
    void excessLifeGainCreatesOnlyOneToken() {
        harness.addToBattlefield(player1, new GriffinAerie());
        resolveRevitalize(player1);
        resolveRevitalize(player1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Griffin")).isEqualTo(1);
    }

    @Test
    @DisplayName("Life gained after the end step begins cannot trigger Aerie")
    void lateLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new GriffinAerie());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        resolveRevitalize(player1);

        assertThat(countPermanents(player1, "Griffin")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the controller's life gain qualifies")
    void opponentsLifeGainDoesNotQualify() {
        harness.addToBattlefield(player1, new GriffinAerie());
        resolveRevitalize(player2);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Griffin")).isZero();
    }

    @Test
    @DisplayName("Each Aerie creates its own token")
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new GriffinAerie());
        harness.addToBattlefield(player1, new GriffinAerie());
        resolveRevitalize(player1);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Griffin")).isEqualTo(2);
    }

    private void resolveRevitalize(Player player) {
        harness.setLibrary(player, List.of(new GriffinAerie()));
        harness.setHand(player, List.of(new Revitalize()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player, 0);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
