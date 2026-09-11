package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontPorchSentries.class, GrizzlyBears.class, LightningBolt.class})
class FrontPorchSentriesTest extends BaseCardTest {

    @Test
    @DisplayName("When Front Porch Sentries dies, target opponent creature gets -1/-1")
    void deathTriggerDebuffsOpponentCreature() {
        harness.addToBattlefield(player1, new FrontPorchSentries());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        killSentries();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(targetId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Front Porch Sentries' death trigger cannot target your creature")
    void deathTriggerOnlyTargetsOpponentCreatures() {
        harness.addToBattlefield(player1, new FrontPorchSentries());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");

        killSentries();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreatureId)
                .doesNotContain(ownCreatureId);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new FrontPorchSentries());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        killSentries();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    private void killSentries() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID sentriesId = harness.getPermanentId(player1, "Front Porch Sentries");
        harness.castInstant(player2, 0, sentriesId);
        harness.passBothPriorities();
    }
}
