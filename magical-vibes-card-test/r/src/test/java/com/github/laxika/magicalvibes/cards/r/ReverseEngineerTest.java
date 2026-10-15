package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReverseEngineer.class, Ornithopter.class})
class ReverseEngineerTest extends BaseCardTest {

    @Test
    void resolvingDrawsThreeCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new ReverseEngineer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        harness.assertInGraveyard(player1, "Reverse Engineer");
    }
    @Test
    void improvisePaysGenericCostWithSummoningSickArtifacts() {
        var first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        var second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        var third = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setHand(player1, List.of(new ReverseEngineer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Reverse Engineer");
    }

    @Test
    void improviseCannotReplaceBlueMana() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new ReverseEngineer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(artifact.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedArtifactCannotPayForImprovise() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.setHand(player1, List.of(new ReverseEngineer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(artifact.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentsArtifactCannotPayForImprovise() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReverseEngineer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(artifact.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sameArtifactCannotPayTwice() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new ReverseEngineer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(artifact.getId(), artifact.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
