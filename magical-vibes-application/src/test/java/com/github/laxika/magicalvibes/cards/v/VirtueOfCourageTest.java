package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EmberethBlaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirtueOfCourage.class, EmberethBlaze.class, GrizzlyBears.class, Island.class, Shock.class})
class VirtueOfCourageTest extends BaseCardTest {

    @Test
    void adventureDealsTwoDamageAndExilesTheCard() {
        VirtueOfCourage card = new VirtueOfCourage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void noncombatDamageOffersExilingThatManyCardsAndPlayingThem() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var first = new GrizzlyBears();
        var second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(first.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(second.getId())).isEqualTo(player1.getId());
    }

    @Test
    void decliningTheTriggerLeavesTheLibraryUnchanged() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }
}
