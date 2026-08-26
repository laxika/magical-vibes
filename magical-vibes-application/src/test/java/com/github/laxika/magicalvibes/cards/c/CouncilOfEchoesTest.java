package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CouncilOfEchoes.class, GrizzlyBears.class, Island.class, Shock.class})
class CouncilOfEchoesTest extends BaseCardTest {

    @Test
    @DisplayName("Descend 4 returns up to one other nonland permanent")
    void descendFourReturnsTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Island(), new GrizzlyBears(), new Island()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(targetId, player1.getId());
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Council of Echoes");
    }

    @Test
    @DisplayName("Descend 4 does not count nonpermanent cards")
    void nonpermanentCardsDoNotEnableDescend() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Island(), new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The source and lands cannot be chosen")
    void sourceAndLandsAreNotLegalTargets() {
        harness.addToBattlefield(player1, new Island());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Island(), new GrizzlyBears(), new Island()));
        harness.setHand(player1, List.of(new CouncilOfEchoes()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Council of Echoes");
        harness.assertOnBattlefield(player1, "Island");
    }
}
