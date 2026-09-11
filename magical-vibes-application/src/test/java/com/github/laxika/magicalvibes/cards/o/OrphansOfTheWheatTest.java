package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrphansOfTheWheat.class, GrizzlyBears.class})
class OrphansOfTheWheatTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts to tap untapped creatures and boosts Orphans of the Wheat")
    void attackTriggerTapsCreaturesAndBoostsSource() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());
        Permanent firstCreature = addReadyCreature(new GrizzlyBears());
        Permanent secondCreature = addReadyCreature(new GrizzlyBears());

        declareAttack(orphans);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, orphans)).isEqualTo(3);
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the source unchanged")
    void choosingNoCreaturesDoesNotBoostSource() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());
        Permanent creature = addReadyCreature(new GrizzlyBears());

        declareAttack(orphans);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, orphans)).isEqualTo(1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent orphans = addReadyCreature(new OrphansOfTheWheat());
        Permanent creature = addReadyCreature(new GrizzlyBears());

        declareAttack(orphans);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, orphans)).isEqualTo(2);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void declareAttack(Permanent orphans) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(orphans)));
    }
}
