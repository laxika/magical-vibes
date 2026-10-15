package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeafDancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SacredRites.class, LeafDancer.class})
class SacredRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures +0/+1 for each card discarded")
    void boostsOwnCreaturesByDiscardCount() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);
        harness.setHand(player1, List.of(new SacredRites(), new LeafDancer(), new LeafDancer(), new LeafDancer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Choosing zero cards leaves creatures unchanged")
    void canDiscardZeroCards() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new SacredRites(), new LeafDancer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new SacredRites(), new LeafDancer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves with an empty hand after casting without requiring a discard choice")
    void resolvesWithNoCardsToDiscard() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new SacredRites()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can discard the entire remaining hand even without controlling creatures")
    void canDiscardAllCardsWithoutCreatures() {
        LeafDancer firstDiscard = new LeafDancer();
        LeafDancer secondDiscard = new LeafDancer();
        harness.setHand(player1, List.of(new SacredRites(), firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard, secondDiscard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent existingCreature = addCreature(player1);
        harness.setHand(player1, List.of(new SacredRites(), new LeafDancer(), new LeafDancer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent newCreature = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Discards the chosen card during resolution rather than as a casting cost")
    void choosesWhichCardToDiscardOnResolution() {
        Permanent creature = addCreature(player1);
        LeafDancer keptCard = new LeafDancer();
        SacredRites discardedCard = new SacredRites();
        harness.setHand(player1, List.of(new SacredRites(), keptCard, discardedCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, discardedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discardedCard);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new LeafDancer());
    }
}
