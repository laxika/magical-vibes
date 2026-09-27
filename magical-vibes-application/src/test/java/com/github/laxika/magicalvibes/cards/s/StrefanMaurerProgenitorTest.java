package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrefanMaurerProgenitor.class, CaptivatingVampire.class})
class StrefanMaurerProgenitorTest extends BaseCardTest {

    @Test
    void createsOneBloodForEachPlayerWhoLostLife() {
        harness.addToBattlefield(player1, new StrefanMaurerProgenitor());
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        gd.lifeLostThisTurn.put(player2.getId(), 2);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Blood")).hasSize(2);
    }

    @Test
    void doesNotCreateBloodWhenNoPlayerLostLife() {
        harness.addToBattlefield(player1, new StrefanMaurerProgenitor());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    void sacrificesTwoBloodToPutVampireOntoBattlefieldTappedAndAttackingWithIndestructible() {
        Permanent strefan = addCreatureReady(player1, new StrefanMaurerProgenitor());
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        advanceToEndStep();

        harness.setHand(player1, List.of(new CaptivatingVampire()));
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS));
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(strefan)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Blood")).isEmpty();
        Permanent vampire = findPermanent(player1, "Captivating Vampire");
        assertThat(vampire.isTapped()).isTrue();
        assertThat(vampire.isAttacking()).isTrue();
        assertThat(vampire.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
