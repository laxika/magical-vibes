package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BoulderJockey.class)
class BoulderJockeyTest extends BaseCardTest {

    @Test
    void castingConsumesOneLandDropInAdditionToMana() {
        harness.setHand(player1, List.of(new BoulderJockey()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void cannotCastWithoutAnAvailableLandDrop() {
        harness.setHand(player1, List.of(new BoulderJockey()));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingMayCreateAndLaterSacrificeABoulder() {
        Permanent jockey = addCreatureReady(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent boulder = findPermanents(player1, "Boulder").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(boulder.isTapped()).isTrue();
        // Auto-pass has already cleared combat state; Jockey and its attacking
        // Boulder dealt 4 and 3 damage before this assertion.
        harness.assertLife(player2, 13);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Boulder")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jockey);
    }

    @Test
    void decliningTheAttackPaymentCreatesNoBoulder() {
        addCreatureReady(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(findPermanents(player1, "Boulder")).isEmpty();
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = addCreatureReady(player, new BoulderJockey());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    @Test
    void acceptingAttackPaymentWithoutARemainingLandDropCreatesNoBoulder() {
        addCreatureReady(player1);
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(findPermanents(player1, "Boulder")).isEmpty();
    }

    @Test
    void twoAttackTriggersCannotSpendTheSameLandDrop() {
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(findPermanents(player1, "Boulder")).hasSize(1);
        harness.assertLife(player2, 9);
    }
}
