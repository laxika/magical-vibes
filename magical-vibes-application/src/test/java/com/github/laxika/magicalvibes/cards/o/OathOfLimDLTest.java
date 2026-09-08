package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfLimDL.class, BalduvianBears.class, Forest.class, Incinerate.class})
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
        harness.addToBattlefield(player1, new BalduvianBears());
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
        harness.assertOnBattlefield(player1, "Balduvian Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Forest")).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Damage dealt to the controller triggers the life-loss ability")
    void damageToControllerTriggersLifeLossAbility() {
        harness.addToBattlefield(player1, new OathOfLimDL());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ChoiceContext.OathOfLimDulPenaltyChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Balduvian Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Controller may sacrifice another permanent (including a land) instead of discarding")
    void maySacrificeOtherPermanentIncludingLand() {
        harness.addToBattlefield(player1, new OathOfLimDL());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BalduvianBears()));
        harness.setLife(player1, 20);

        loseLife(2);

        UUID forestId = forest.getId();
        harness.handleListChoice(player1, ChoiceContext.OathOfLimDulPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player1, forestId);

        // Second life point: only discard remains (Oath can't be sacrificed) — auto-starts discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertOnBattlefield(player1, "Oath of Lim-Dûl");
    }

    @Test
    @DisplayName("Cannot sacrifice Oath of Lim-Dûl itself to satisfy the trigger")
    void cannotSacrificeSelf() {
        Permanent oath = harness.addToBattlefieldAndReturn(player1, new OathOfLimDL());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
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
    @DisplayName("A source that left the battlefield is no longer excluded from the choice")
    void sourceLeavingBattlefieldBeforeResolutionDoesNotExcludeOtherPermanents() {
        Permanent oath = harness.addToBattlefieldAndReturn(player1, new OathOfLimDL());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 1, "test"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, oath));
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Oath of Lim-Dûl");
        harness.assertInGraveyard(player1, "Balduvian Bears");
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
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), amount, "test"));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(amount);
        harness.passBothPriorities();
    }
}
