package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrathOfLeknif.class, GrizzlyBears.class, Island.class})
class WrathOfLeknifTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesWithoutAllowingRegeneration() {
        Permanent player1Bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        player1Bears.setRegenerationShield(1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castWrath();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void untapsUpToFourLandsYouControl() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        }

        castWrath();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);

        List<java.util.UUID> selected = findPermanents(player1, "Island").stream()
                .map(Permanent::getId)
                .limit(4)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, selected);

        assertThat(findPermanents(player1, "Island")).hasSize(6)
                .filteredOn(Permanent::isTapped).hasSize(2);
    }

    private void castWrath() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromHand(player1, new WrathOfLeknif(), "{1}{W}{W}{U}");
    }
}
