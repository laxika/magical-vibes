package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.m.MercadianAtlas;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimidDrake.class, FreshVolunteers.class, MercadianAtlas.class})
class TimidDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to hand when another creature the controller casts enters")
    void bouncesSelfOnAllyCreatureEntering() {
        harness.addToBattlefield(player1, new TimidDrake());

        harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}");

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof TimidDrake);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c instanceof TimidDrake);
    }

    @Test
    @DisplayName("Returns itself to hand when an opponent's creature enters")
    void bouncesSelfOnOpponentCreatureEntering() {
        harness.addToBattlefield(player1, new TimidDrake());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c instanceof TimidDrake);
    }

    @Test
    @DisplayName("Does not trigger on its own entry")
    void doesNotTriggerOnSelfEntering() {
        harness.castFromHand(player1, new TimidDrake(), "{2}{U}");

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof TimidDrake);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature artifact enters")
    void doesNotTriggerOnNonCreatureEntering() {
        harness.addToBattlefield(player1, new TimidDrake());

        harness.castFromHand(player1, new MercadianAtlas(), "{5}");

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof TimidDrake);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c instanceof TimidDrake);
    }

    @Test
    @DisplayName("Returns itself when another creature enters without being cast")
    void bouncesSelfOnCreaturePutOntoBattlefield() {
        harness.addToBattlefield(player1, new TimidDrake());

        harness.enterBattlefieldAndReturn(player1, new FreshVolunteers());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof TimidDrake);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c instanceof TimidDrake);
    }

    @Test
    @DisplayName("Returns to its owner's hand when another player controls it")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        TimidDrake drakeCard = new TimidDrake();
        drakeCard.setOwnerId(player1.getId());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, drakeCard);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(drake.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(drakeCard.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(drakeCard.getId()));
    }
}
