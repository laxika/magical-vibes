package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestralStatue.class, GrizzlyBears.class, Island.class})
class AncestralStatueTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only nonland permanents you control, including itself")
    void etbOffersControlledNonlandPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAncestralStatue();

        UUID statueId = harness.getPermanentId(player1, "Ancestral Statue");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);

        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(statueId, creature.getId());
        assertThat(choice.validIds()).doesNotContain(land.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("The chosen nonland permanent returns to its owner's hand")
    void chosenPermanentReturnsToHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castAncestralStatue();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ancestral Statue");
    }

    @Test
    @DisplayName("It can return itself when it is the only nonland permanent you control")
    void canReturnItself() {
        castAncestralStatue();

        UUID statueId = harness.getPermanentId(player1, "Ancestral Statue");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(statueId);

        harness.handlePermanentChosen(player1, statueId);

        harness.assertInHand(player1, "Ancestral Statue");
        harness.assertNotOnBattlefield(player1, "Ancestral Statue");
    }

    private void castAncestralStatue() {
        harness.setHand(player1, List.of(new AncestralStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
