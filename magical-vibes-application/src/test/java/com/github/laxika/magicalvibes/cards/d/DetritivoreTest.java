package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Detritivore.class, Forest.class, GhostQuarter.class})
class DetritivoreTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualNonbasicLandCardsInOpponentsGraveyards() {
        Permanent detritivore = harness.addToBattlefieldAndReturn(player1, new Detritivore());
        harness.setGraveyard(player1, List.of(new GhostQuarter()));
        harness.setGraveyard(player2, List.of(
                new GhostQuarter(), new GhostQuarter(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, detritivore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, detritivore)).isEqualTo(2);
    }

    @Test
    void timeCounterTriggerDestroysTargetNonbasicLand() {
        Detritivore card = suspendCard(1);
        harness.addToBattlefield(player2, new GhostQuarter());
        var targetId = harness.getPermanentId(player2, "Ghost Quarter");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void suspendXCannotBeZero() {
        Detritivore card = new Detritivore();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    private Detritivore suspendCard(int xValue) {
        Detritivore card = new Detritivore();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 3);
        harness.activateHandAbility(player1, 0, null, xValue);
        return card;
    }
}
