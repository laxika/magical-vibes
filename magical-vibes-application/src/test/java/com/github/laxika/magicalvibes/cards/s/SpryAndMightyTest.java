package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThoughtweftImbuer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpryAndMighty.class, SizzlingChangeling.class, ThoughtweftImbuer.class, Forest.class})
class SpryAndMightyTest extends BaseCardTest {

    @Test
    void choosesTwoCreaturesDrawsByPowerDifferenceAndGrantsTrample() {
        Permanent smaller = addCreature(2);
        Permanent larger = addCreature(5);
        Permanent unchosen = addCreature(1);
        harness.setHand(player1, List.of(new SpryAndMighty()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(smaller.getId(), larger.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, smaller)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, smaller)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, smaller, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, larger)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, larger)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, larger, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, unchosen)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, unchosen, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void doesNothingWhenTwoCreaturesCannotBeChosen() {
        Permanent creature = addCreature(2);
        harness.setHand(player1, List.of(new SpryAndMighty()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void equalPowersStillGrantTrampleWithoutDrawing() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SizzlingChangeling());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SizzlingChangeling());
        castSpryAndMighty();

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        for (Permanent creature : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    void usesEffectiveNegativePowerAndExpiresAtEndOfTurn() {
        Permanent smaller = harness.addToBattlefieldAndReturn(player1, new ThoughtweftImbuer());
        Permanent larger = harness.addToBattlefieldAndReturn(player1, new SizzlingChangeling());
        smaller.setPowerModifier(-2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        castSpryAndMighty();

        harness.handleMultiplePermanentsChosen(player1, List.of(larger.getId(), smaller.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gqs.getEffectivePower(gd, smaller)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, smaller)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, larger)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, larger)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, smaller, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, larger, Keyword.TRAMPLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, smaller)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, smaller)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, larger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, larger)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, smaller, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, larger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void requiresTwoDistinctControlledCreaturesAndExcludesLands() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SizzlingChangeling());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SizzlingChangeling());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SizzlingChangeling());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castSpryAndMighty();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void doesNothingWithNoControlledCreatures() {
        harness.addToBattlefield(player2, new SizzlingChangeling());
        castSpryAndMighty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castSpryAndMighty() {
        harness.setHand(player1, List.of(new SpryAndMighty()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private Permanent addCreature(int power) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SizzlingChangeling());
        TestCards.mutableCard(creature).setPower(power);
        TestCards.mutableCard(creature).setToughness(power);
        return creature;
    }
}
