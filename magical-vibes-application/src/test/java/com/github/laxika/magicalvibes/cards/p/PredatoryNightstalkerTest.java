package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldenBear;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatoryNightstalker.class, BearCub.class, GoldenBear.class})
class PredatoryNightstalkerTest extends BaseCardTest {

    private void castNightstalker() {
        harness.castFromHand(player1, new PredatoryNightstalker(), "{3}{B}{B}");
    }

    @Test
    @DisplayName("Resolving prompts the may ability")
    void resolvingPromptsMayAbility() {
        castNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("The may ability only offers opponents as valid targets")
    void targetFilterExcludesController() {
        castNightstalker();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Accepting makes target opponent choose a creature to sacrifice")
    void acceptingLetsOpponentChooseSacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.addToBattlefield(player2, new GoldenBear());

        castNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        // Opponent sacrifices the Bear Cub
        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertNotOnBattlefield(player2, "Bear Cub");
        harness.assertOnBattlefield(player2, "Golden Bear");
        harness.assertInGraveyard(player2, "Bear Cub");
    }

    @Test
    @DisplayName("Declining the may ability leaves the opponent's creatures untouched")
    void decliningLeavesCreaturesUntouched() {
        harness.addToBattlefield(player2, new BearCub());

        castNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Bear Cub");
        harness.assertOnBattlefield(player1, "Predatory Nightstalker");
    }

    @Test
    @DisplayName("Accepting with no creatures resolves without a sacrifice choice")
    void acceptingWithNoCreaturesDoesNothing() {
        castNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Predatory Nightstalker");
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("Only creatures are offered for the sacrifice")
    void sacrificeChoiceExcludesNoncreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new GoldenBear());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(bears.getId(), giant.getId())
                .doesNotContain(forest.getId());

        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertNotOnBattlefield(player2, "Bear Cub");
        harness.assertOnBattlefield(player2, "Golden Bear");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Opponent with one creature sacrifices it automatically")
    void opponentWithOneCreatureSacrificesAutomatically() {
        harness.addToBattlefield(player2, new BearCub());

        castNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Bear Cub");
        harness.assertInGraveyard(player2, "Bear Cub");
    }
}
