package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmogbelcherChariot.class, SerraAngel.class, GrizzlyBears.class})
class SmogbelcherChariotTest extends BaseCardTest {

    @Test
    void attackTriggerTargetsOnlyCreatureThatCrewedItAndGrantsChosenKeywordIndefinitely() {
        addReadyChariot();
        Permanent crewer = addReadyCreature(new SerraAngel());
        Permanent bystander = addReadyCreature(new GrizzlyBears());

        crewChariot(crewer);
        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(crewer.getId()).doesNotContain(bystander.getId());

        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "MENACE");

        assertThat(gqs.hasKeyword(gd, crewer, Keyword.MENACE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crewer, Keyword.MENACE)).isTrue();
    }

    @Test
    void attackTriggerDoesNotExistWithoutAValidCrewer() {
        addReadyChariot();
        Permanent crewer = addReadyCreature(new SerraAngel());
        crewChariot(crewer);
        gd.playerBattlefields.get(player1.getId()).remove(crewer);
        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyChariot() {
        Permanent chariot = harness.addToBattlefieldAndReturn(player1, new SmogbelcherChariot());
        chariot.setSummoningSick(false);
        return chariot;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void crewChariot(Permanent crewer) {
        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        harness.passBothPriorities();
    }
}
