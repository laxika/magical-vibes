package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CosisRavagerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may deal 1 damage to the chosen player")
    void landfallDealsDamageToPlayer() {
        addRavager();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Landfall may deal 1 damage to a planeswalker")
    void landfallDealsDamageToPlaneswalker() {
        addRavager();
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(liliana.getId());
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining landfall deals no damage")
    void decliningLandfallDealsNoDamage() {
        addRavager();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature is not offered as a landfall target")
    void landfallRejectsCreatureTarget() {
        addRavager();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTriggerLandfall() {
        addRavager();
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent addRavager() {
        return harness.addToBattlefieldAndReturn(player1, new CosisRavager());
    }
}
