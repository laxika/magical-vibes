package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TilonallisSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {X}{R} creates X tapped and attacking Elemental tokens")
    void payingCreatesTappedAttackingElementals() {
        addCreatureReady(player1, new TilonallisSummoner());
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        List<Permanent> tokens = findPermanents(player1, "Elemental");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(token -> token.isTapped() && token.isAttacking());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Created Elementals are exiled at the next end step without the city's blessing")
    void createdElementalsAreExiledAtNextEndStepWithoutBlessing() {
        addCreatureReady(player1, new TilonallisSummoner());
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Created Elementals remain at the next end step with the city's blessing")
    void createdElementalsRemainAtNextEndStepWithBlessing() {
        addCreatureReady(player1, new TilonallisSummoner());
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }
}
