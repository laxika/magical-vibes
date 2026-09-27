package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesecrateReality.class, GrizzlyBears.class, LlanowarElves.class})
class DesecrateRealityTest extends BaseCardTest {

    @Test
    void exilesOneEvenManaValuePermanentPerOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(first.getId()), ManaColor.GREEN, 7);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(second.getId());
    }

    @Test
    void rejectsOddManaValueAndControllerPermanents() {
        Permanent odd = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(odd.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(own.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adamantReturnsAnOddPermanentFromGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        cast(List.of(target.getId()), ManaColor.COLORLESS, 7);

        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Llanowar Elves");
    }

    @Test
    void adamantDoesNotReturnPermanentWithoutThreeColorlessMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        cast(List.of(target.getId()), ManaColor.GREEN, 7);

        assertThat(findPermanents(player1, "Llanowar Elves")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Llanowar Elves");
    }

    private void cast(List<java.util.UUID> targetIds, ManaColor color, int amount) {
        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, color, amount);
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
