package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZukosExile;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaiLiIndoctrination.class, Forest.class, GrizzlyBears.class, Peek.class,
        Shock.class, ZukosExile.class})
class DaiLiIndoctrinationTest extends BaseCardTest {

    @Test
    @DisplayName("The discard mode lets you choose a nonland permanent from the opponent's hand")
    void discardsChosenNonlandPermanent() {
        Card bear = new GrizzlyBears();
        Card peek = new Peek();
        Card forest = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(bear, peek, forest)));

        cast(0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Peek");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("The Earthbend mode animates a land and puts two +1/+1 counters on it")
    void earthbendsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        cast(1, land.getId());

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Earthbend can target only a land controlled by the caster")
    void earthbendRejectsLandControlledByOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DaiLiIndoctrination()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardModeRejectsCasterAsTarget() {
        harness.setHand(player1, List.of(new DaiLiIndoctrination()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void handWithoutNonlandPermanentsIsRevealedWithoutDiscarding() {
        harness.setHand(player2, List.of(new Peek(), new Forest(), new DaiLiIndoctrination()));

        cast(0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player2, "Peek");
        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Dai Li Indoctrination");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gameLogContains("reveals their hand")).isTrue();
    }

    @Test
    void emptyHandDoesNotRequireAChoice() {
        harness.setHand(player2, List.of());

        cast(0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dai Li Indoctrination");
    }

    @Test
    void earthbendRejectsNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DaiLiIndoctrination()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void earthbendingAgainKeepsExistingCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        cast(1, land.getId());
        cast(1, land.getId());

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
    }

    @Test
    void earthbendedLandReturnsTappedAfterDying() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        cast(1, land.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, land.getId());
        resolveAllTriggers();

        assertReturnedAsOrdinaryLand(land);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void earthbendedLandReturnsTappedAfterExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        cast(1, land.getId());
        harness.setHand(player1, List.of(new ZukosExile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, land.getId());
        resolveAllTriggers();

        assertReturnedAsOrdinaryLand(land);
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    private void assertReturnedAsOrdinaryLand(Permanent original) {
        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DaiLiIndoctrination()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, mode, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
