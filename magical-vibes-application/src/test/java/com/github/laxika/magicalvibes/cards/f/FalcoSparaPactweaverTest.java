package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalcoSparaPactweaver.class, GrizzlyBears.class})
class FalcoSparaPactweaverTest extends BaseCardTest {

    @Test
    void entersWithShieldCounter() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());

        assertThat(falco.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void castsSpellFromLibraryTopByRemovingCounterFromCreature() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1, List.of(falco.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(falco.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void requiresCounterPaymentForSpellFromLibraryTop() {
        harness.addToBattlefield(player1, new FalcoSparaPactweaver());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }
}
