package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObscuraConfluence.class, GrizzlyBears.class, SerraAngel.class, Plains.class})
class ObscuraConfluenceTest extends BaseCardTest {

    @Test
    void repeatedAbilityRemovalModeSetsCreatureToOneOneUntilEndOfTurn() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new ObscuraConfluence()));

        cast(new int[]{0, 0, 0}, List.of(angel.getId(), angel.getId(), angel.getId()));

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    void conniveModeDrawsAndAddsCounterAfterDiscardingNonland() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObscuraConfluence(), new SerraAngel()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(new int[]{1, 0, 0}, List.of(creature.getId(), creature.getId(), creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        List<Card> hand = gd.playerHands.get(player1.getId());
        harness.handleCardChosen(player1, hand.indexOf(hand.stream()
                .filter(card -> card.getName().equals("Serra Angel"))
                .findFirst().orElseThrow()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    void targetPlayerReturnsCreatureFromTheirGraveyardToHand() {
        Card creature = new GrizzlyBears();
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ObscuraConfluence()));

        cast(new int[]{2, 0, 0}, List.of(player2.getId(), battlefieldCreature.getId(), battlefieldCreature.getId()));

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void creatureModesRejectNoncreatureTargets() {
        harness.setHand(player1, List.of(new ObscuraConfluence()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 0),
                List.of(player1.getId(), player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentCreatureControllerDrawsAndDiscardsForConnive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObscuraConfluence(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new SerraAngel()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        cast(new int[]{0, 0, 1}, List.of(creature.getId(), creature.getId(), creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Serra Angel", "Grizzly Bears");
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void discardingLandToConniveDoesNotAddCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObscuraConfluence(), new Plains()));
        harness.setLibrary(player1, List.of(new SerraAngel()));

        cast(new int[]{0, 0, 1}, List.of(creature.getId(), creature.getId(), creature.getId()));
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Plains");
        harness.assertInHand(player1, "Serra Angel");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void repeatedConniveResolvesThreeSeparateDrawDiscardChoices() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObscuraConfluence(), new SerraAngel()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        cast(new int[]{1, 1, 1}, List.of(creature.getId(), creature.getId(), creature.getId()));

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
            harness.handleCardChosen(player1, 0);
        }

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedReturnLetsTargetPlayerChooseAndIgnoresNoncreatureCards() {
        Card noncreature = new ObscuraConfluence();
        harness.setGraveyard(player2, List.of(noncreature, new GrizzlyBears(), new SerraAngel()));
        harness.setHand(player1, List.of(new ObscuraConfluence()));

        cast(new int[]{2, 2, 2}, List.of(player2.getId(), player2.getId(), player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Serra Angel", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void allThreeModesResolveInOrderAndCanReturnTheCardDiscardedToConnive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player1, List.of(new ObscuraConfluence(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setGraveyard(player1, List.of());

        cast(new int[]{0, 1, 2}, List.of(creature.getId(), creature.getId(), player1.getId()));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedAbilityRemovalCanAffectThreeDifferentCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObscuraConfluence()));

        cast(new int[]{0, 0, 0}, List.of(first.getId(), second.getId(), third.getId()));

        for (Permanent creature : List.of(first, second, third)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        }
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        addMana();
        harness.castModalInstant(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices), targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
