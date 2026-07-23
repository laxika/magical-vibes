package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OathOfLimDLTest extends BaseCardTest {

    @Test
    @DisplayName("Losing life with no other permanents and empty hand does nothing")
    void lifeLossIgnoredWhenNothingToGive() {
        Permanent oath = harness.addToBattlefieldAndReturn(player1, new OathOfLimDL());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());

        loseLife(2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oath);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller may discard instead of sacrificing for each life lost")
    void mayDiscardPerLifeLost() {
        harness.addToBattlefield(player1, new OathOfLimDL());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);

        loseLife(2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ChoiceContext.OathOfLimDulPenaltyChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, ChoiceContext.OathOfLimDulPenaltyChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Forest")).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Controller may sacrifice another permanent (including a land) instead of discarding")
    void maySacrificeOtherPermanentIncludingLand() {
        harness.addToBattlefield(player1, new OathOfLimDL());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        loseLife(2);

        UUID forestId = forest.getId();
        harness.handleListChoice(player1, ChoiceContext.OathOfLimDulPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player1, forestId);

        // Second life point: only discard remains (Oath can't be sacrificed) — auto-starts discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Oath of Lim-Dûl"));
    }

    @Test
    @DisplayName("Cannot sacrifice Oath of Lim-Dûl itself to satisfy the trigger")
    void cannotSacrificeSelf() {
        Permanent oath = harness.addToBattlefieldAndReturn(player1, new OathOfLimDL());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        loseLife(2);

        // Only sacrifice is available (empty hand) — Oath is not among legal choices.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        // Second life point: only Oath remains — nothing legal to sacrifice and empty hand → skip.
        assertThat(gd.playerBattlefields.get(player1.getId())).containsOnly(oath);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("{B}{B}: Draw a card")
    void activatedAbilityDrawsACard() {
        harness.addToBattlefield(player1, new OathOfLimDL());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    private void loseLife(int amount) {
        harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), amount, "test");
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(amount);
        harness.passBothPriorities();
    }
}
