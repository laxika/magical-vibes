package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearingBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage, then deals 3 damage to the creature's controller when it dies")
    void dealsThreeDamageWhenTheTargetDies() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        castSearingBlood(harness.getPermanentId(player2, "Fugitive Wizard"));
        resolveStack();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Triggers if the damaged creature dies later in the same turn")
    void triggersWhenTargetDiesLaterInTheTurn() {
        GrizzlyBears card = new GrizzlyBears();
        card.setToughness(3);
        harness.addToBattlefield(player2, card);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castSearingBlood(targetId);
        resolveStack();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not register the delayed trigger when the target is gone at resolution")
    void doesNotTriggerWhenTargetIsGoneAtResolution() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new SearingBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        UUID targetId = harness.getPermanentId(player2, "Fugitive Wizard");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    private void castSearingBlood(UUID targetId) {
        harness.setHand(player1, List.of(new SearingBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, targetId);
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }
}
