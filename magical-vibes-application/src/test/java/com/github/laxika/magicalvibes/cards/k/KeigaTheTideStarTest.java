package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KeigaTheTideStarTest extends BaseCardTest {

    @Test
    @DisplayName("When Keiga dies, its controller gains control of target creature permanently")
    void diesGainsControlOfTargetCreature() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID keigaId = harness.getPermanentId(player1, "Keiga, the Tide Star");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player2, 0, keigaId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death trigger targets creatures only, not lands")
    void deathTriggerOffersCreaturesOnly() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID keigaId = harness.getPermanentId(player1, "Keiga, the Tide Star");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.castInstant(player2, 0, keigaId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bearsId)
                .doesNotContain(forestId);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
