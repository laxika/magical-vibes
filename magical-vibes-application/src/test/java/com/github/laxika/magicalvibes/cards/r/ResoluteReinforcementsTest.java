package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResoluteReinforcements.class, BurstLightning.class})
class ResoluteReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Soldier token")
    void enteringCreatesSoldierToken() {
        harness.castFromHand(player1, new ResoluteReinforcements(), "{1}{W}");
        resolveAllTriggers();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's main phase")
    void flashAllowsCastingDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ResoluteReinforcements(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The Soldier is created only when the enter trigger resolves")
    void tokenCreationUsesTheStack() {
        harness.castFromHand(player1, new ResoluteReinforcements(), "{1}{W}");
        harness.assertNotOnBattlefield(player1, "Soldier");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Resolute Reinforcements");
        harness.assertNotOnBattlefield(player1, "Soldier");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(findPermanent(player1, "Soldier").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Soldier");
    }

    @Test
    @DisplayName("Removing Reinforcements in response does not stop its Soldier trigger")
    void triggerResolvesAfterSourceDies() {
        harness.castFromHand(player1, new ResoluteReinforcements(), "{1}{W}");
        harness.passBothPriorities();
        Permanent reinforcements = findPermanent(player1, "Resolute Reinforcements");
        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, reinforcements.getId());

        harness.assertInGraveyard(player1, "Resolute Reinforcements");
        harness.assertNotOnBattlefield(player1, "Soldier");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soldier");
        harness.assertNotOnBattlefield(player2, "Soldier");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Flash permits casting during the opponent's end step")
    void flashWorksOutsideMainPhases() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new ResoluteReinforcements(), "{1}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Resolute Reinforcements");
        harness.assertOnBattlefield(player1, "Soldier");
        harness.assertNotOnBattlefield(player2, "Soldier");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
