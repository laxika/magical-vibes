package com.github.laxika.magicalvibes.cards.a;

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

class ArchonOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("When Archon of Justice dies, exile target permanent (a land)")
    void diesExilesTargetLand() {
        harness.addToBattlefield(player1, new ArchonOfJustice());
        harness.addToBattlefield(player2, new Forest());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID archonId = harness.getPermanentId(player1, "Archon of Justice");
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.castInstant(player2, 0, archonId);
        harness.passBothPriorities(); // Flame Javelin resolves → Archon dies → death trigger awaits target

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Death trigger can target any permanent, including non-creatures")
    void targetFilterAllowsAnyPermanent() {
        harness.addToBattlefield(player1, new ArchonOfJustice());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID archonId = harness.getPermanentId(player1, "Archon of Justice");
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player2, 0, archonId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(forestId, bearsId);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
