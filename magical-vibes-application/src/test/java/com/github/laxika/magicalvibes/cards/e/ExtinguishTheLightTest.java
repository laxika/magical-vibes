package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtinguishTheLight.class, GrizzlyBears.class, AirElemental.class, LilianaVess.class,
        RodOfRuin.class})
class ExtinguishTheLightTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with mana value 3 or less and gains 3 life")
    void destroysLowManaValueCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Destroys a creature with mana value greater than 3 without gaining life")
    void destroysHighManaValueCreatureWithoutLifeGain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(target);

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Destroys a planeswalker")
    void destroysPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        target.setCounterCount(CounterType.LOYALTY, 5);

        cast(target);

        harness.assertInGraveyard(player2, "Liliana Vess");
    }

    @Test
    @DisplayName("Rejects a noncreature, nonplaneswalker permanent")
    void rejectsOtherPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    private void cast(Permanent target) {
        prepareCard();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new ExtinguishTheLight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
