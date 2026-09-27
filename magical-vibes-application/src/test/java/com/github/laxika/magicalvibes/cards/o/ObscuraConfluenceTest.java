package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ObscuraConfluence.class, GrizzlyBears.class, SerraAngel.class})
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
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 0),
                List.of(player1.getId(), player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
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
