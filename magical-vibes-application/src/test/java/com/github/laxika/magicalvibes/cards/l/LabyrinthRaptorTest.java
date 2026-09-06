package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LabyrinthRaptor.class, GiantSpider.class, GrizzlyBears.class})
class LabyrinthRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("A menace creature becoming blocked makes the defending player choose a blocker to sacrifice")
    void defendingPlayerSacrificesAChosenBlocker() {
        addCreatureReady(player1, new LabyrinthRaptor());
        Permanent firstBlocker = addCreatureReady(player2, new GiantSpider());
        Permanent secondBlocker = addCreatureReady(player2, new GiantSpider());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(firstBlocker.getId(), secondBlocker.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(firstBlocker.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstBlocker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondBlocker.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstBlocker.getId()));
    }

    @Test
    @DisplayName("The activation boosts only creatures you control with menace")
    void activationBoostsMenaceCreaturesOnly() {
        Permanent raptor = addCreatureReady(player1, new LabyrinthRaptor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
    }
}
