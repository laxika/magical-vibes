package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DustElemental.class, GrizzlyBears.class, Island.class})
class DustElementalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets you choose exactly three creatures you control, including Dust Elemental")
    void choosesThreeCreaturesToReturn() {
        UUID firstBearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID secondBearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID thirdBearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DustElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        UUID dustElementalId = harness.getPermanentId(player1, "Dust Elemental");
        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstBearId, secondBearId, thirdBearId, dustElementalId);
        assertThat(choice.maxCount()).isEqualTo(3);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(firstBearId, secondBearId)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1,
                List.of(dustElementalId, firstBearId, secondBearId));

        harness.assertInHand(player1, "Dust Elemental");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB returns all available creatures when you control fewer than three")
    void returnsAllAvailableCreaturesWhenFewerThanThree() {
        harness.setHand(player1, List.of(new DustElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Dust Elemental");
    }
}
