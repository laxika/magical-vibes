package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConsulateCrackdownTest extends BaseCardTest {

    private void castAndResolveCrackdown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ConsulateCrackdown()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles all artifacts controlled by opponents")
    void etbExilesOpponentsArtifactsOnly() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolveCrackdown();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Prophetic Prism"));
    }

    @Test
    @DisplayName("Exiled artifacts return when Consulate Crackdown leaves")
    void exiledArtifactsReturnWhenSourceLeaves() {
        harness.addToBattlefield(player2, new PropheticPrism());

        castAndResolveCrackdown();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID crackdownId = harness.getPermanentId(player1, "Consulate Crackdown");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, crackdownId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Prophetic Prism"));
    }
}
