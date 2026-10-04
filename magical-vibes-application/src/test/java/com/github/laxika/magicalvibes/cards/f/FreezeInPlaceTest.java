package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TuinvaleGuide;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FreezeInPlace.class, TuinvaleGuide.class})
class FreezeInPlaceTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an opponent's creature, puts three stun counters on it, and scries two")
    void tapsStunsAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TuinvaleGuide());
        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the spell's controller")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TuinvaleGuide());
        harness.setHand(player1, java.util.List.of(new FreezeInPlace()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, java.util.List.of(new FreezeInPlace()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    void alreadyTappedCreatureStillGetsThreeAdditionalStunCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TuinvaleGuide());
        target.setTapped(true);
        target.setCounterCount(CounterType.STUN, 1);

        cast(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void stunCountersReplaceThreeUntapsBeforeCreatureUntaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TuinvaleGuide());
        harness.setLibrary(player1, List.of());
        cast(target);

        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(remaining);
        }

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void illegalSoleTargetPreventsScry() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TuinvaleGuide());
        FreezeInPlace spell = new FreezeInPlace();
        harness.setHand(player1, List.of(spell));
        List<Card> libraryBefore =
                List.copyOf(gd.playerDecks.get(player1.getId()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(libraryBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void scryCanPutOneCardOnBottomAndKeepOneOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TuinvaleGuide());
        FreezeInPlace first = new FreezeInPlace();
        TuinvaleGuide second = new TuinvaleGuide();
        FreezeInPlace third = new FreezeInPlace();
        harness.setLibrary(player1, List.of(first, second, third));
        cast(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
