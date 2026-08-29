package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HornedKavuTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a non-targeting choice among red or green creatures you control")
    void etbOffersRedOrGreenCreatures() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID vanguardId = harness.addToBattlefieldAndReturn(player1, new EliteVanguard()).getId();
        harness.addToBattlefield(player2, new RagingGoblin());

        castHornedKavu();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID kavuId = harness.getPermanentId(player1, "Horned Kavu");
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(goblinId, bearsId, kavuId);
        assertThat(choice.validIds()).doesNotContain(vanguardId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen red or green creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castHornedKavu();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Horned Kavu");
    }

    @Test
    @DisplayName("Horned Kavu returns itself when it is the only eligible creature")
    void returnsItselfWhenAlone() {
        castHornedKavu();
        harness.passBothPriorities();

        UUID kavuId = harness.getPermanentId(player1, "Horned Kavu");
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(kavuId);

        harness.handlePermanentChosen(player1, kavuId);

        harness.assertInHand(player1, "Horned Kavu");
        harness.assertNotOnBattlefield(player1, "Horned Kavu");
    }

    private void castHornedKavu() {
        harness.setHand(player1, List.of(new HornedKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
