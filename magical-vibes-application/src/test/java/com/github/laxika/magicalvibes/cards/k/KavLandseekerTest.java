package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavLandseeker.class, Forest.class})
class KavLandseekerTest extends BaseCardTest {

    @Test
    @DisplayName("When Kav Landseeker enters, it creates a Lander token")
    void createsLanderTokenOnEnter() {
        castKavLandseeker();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("The Lander is sacrificed at the end step on its controller's next turn")
    void sacrificesLanderAtEndStepOnControllersNextTurn() {
        castKavLandseeker();
        harness.setHand(player2, List.of());
        Permanent lander = findPermanents(player1, "Lander").getFirst();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Lander")).contains(lander);

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Lander")).contains(lander);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Lander")).contains(lander);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Lander")).isEmpty();
    }

    @Test
    @DisplayName("A newly created Lander can be sacrificed to find a basic land tapped")
    void activatesLanderOnTheTurnItEnters() {
        castKavLandseeker();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new KavLandseeker(), forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(forest);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card instanceof KavLandseeker);
    }

    @Test
    @DisplayName("A Lander can be sacrificed even when there is no basic land to find")
    void activatesLanderWithoutMatchingLand() {
        castKavLandseeker();
        harness.setLibrary(player1, List.of(new KavLandseeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Lander's search may fail to find even when a basic land is available")
    void mayDeclineToFindBasicLand() {
        castKavLandseeker();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The delayed sacrifice still happens after Kav Landseeker leaves the battlefield")
    void sacrificesLanderAfterSourceLeaves() {
        castKavLandseeker();
        Permanent source = findPermanent(player1, "Kav Landseeker");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, source));
        harness.setHand(player2, List.of());

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
    }

    @Test
    @DisplayName("The Lander can be activated in response to its delayed sacrifice trigger")
    void activatesLanderInResponseToDelayedSacrifice() {
        castKavLandseeker();
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castKavLandseeker() {
        harness.castFromHand(player1, new KavLandseeker(), "{3}{R}");
        resolveAllTriggers();
    }
}
