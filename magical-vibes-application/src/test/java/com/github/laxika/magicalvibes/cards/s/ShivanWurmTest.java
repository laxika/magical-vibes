package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

class ShivanWurmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a red or green creature you control, including itself")
    void etbOffersRedOrGreenCreaturesYouControl() {
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new SaprazzanRaider()).getId();
        harness.addToBattlefield(player1, new Island());

        castShivanWurm();
        resolveUntilPermanentChoice();

        GameData gd = harness.getGameData();
        UUID wurmId = harness.getPermanentId(player1, "Shivan Wurm");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);

        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(greenId, redId, wurmId);
        assertThat(choice.validIds()).doesNotContain(blueId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("ETB returns the chosen red or green creature to its owner's hand")
    void chosenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID redId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        harness.addToBattlefield(player1, new SaprazzanRaider());

        castShivanWurm();
        resolveUntilPermanentChoice();
        harness.handlePermanentChosen(player1, redId);

        harness.assertInHand(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Saprazzan Raider");
        harness.assertOnBattlefield(player1, "Shivan Wurm");
    }

    private void castShivanWurm() {
        harness.setHand(player1, List.of(new ShivanWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }

    private void resolveUntilPermanentChoice() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
