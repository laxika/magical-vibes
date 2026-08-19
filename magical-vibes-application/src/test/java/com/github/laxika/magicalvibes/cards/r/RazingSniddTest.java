package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Razing Snidd")
class RazingSniddTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only black or red creatures you control, including itself")
    void etbOffersOnlyBlackOrRedCreaturesYouControl() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new RagingGoblin());

        castRazingSnidd();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID sniddId = harness.getPermanentId(player1, "Razing Snidd");
        assertThat(choice.validIds()).containsExactlyInAnyOrder(goblinId, sniddId);
        assertThat(choice.validIds()).doesNotContain(bearsId);
    }

    @Test
    @DisplayName("ETB returns the chosen creature and makes each player sacrifice a land")
    void etbReturnsCreatureAndSacrificesLandForEachPlayer() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castRazingSnidd();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Raging Goblin"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        UUID landId = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .validIds().getFirst();
        harness.handleMultiplePermanentsChosen(player1, List.of(landId));

        harness.assertInHand(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Razing Snidd");
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isZero();
    }

    private void castRazingSnidd() {
        harness.setHand(player1, List.of(new RazingSnidd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

}
