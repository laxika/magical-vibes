package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
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

@CardUsed({DaiLiIndoctrination.class, Forest.class, GrizzlyBears.class, Peek.class})
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

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DaiLiIndoctrination()));
        addMana();
        harness.castSorcery(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
