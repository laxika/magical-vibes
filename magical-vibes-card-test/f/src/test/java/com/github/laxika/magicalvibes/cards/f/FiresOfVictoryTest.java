package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ExtinguishTheLight;
import com.github.laxika.magicalvibes.cards.j.JayaFieryNegotiator;
import com.github.laxika.magicalvibes.cards.m.MoltenMonstrosity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiresOfVictory.class, MoltenMonstrosity.class, Forest.class,
        JayaFieryNegotiator.class, ExtinguishTheLight.class})
class FiresOfVictoryTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToCardsInControllerHand() {
        Permanent target = addCreatureReady(player2, new MoltenMonstrosity());
        harness.setHand(player1, List.of(
                new FiresOfVictory(), new MoltenMonstrosity(), new MoltenMonstrosity()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void drawsBeforeKickedDamageIsCalculated() {
        harness.setLibrary(player1, List.of(new MoltenMonstrosity()));
        Permanent target = addCreatureReady(player2, new MoltenMonstrosity());
        harness.setHand(player1, List.of(new FiresOfVictory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInHand(player1, "Molten Monstrosity");
    }

    @Test
    void cannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FiresOfVictory()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker");
    }

    @Test
    void unkickedSpellWithEmptyHandDealsNoDamageAndDoesNotDraw() {
        Permanent target = addCreatureReady(player2, new MoltenMonstrosity());
        harness.setHand(player1, List.of(new FiresOfVictory()));
        harness.setLibrary(player1, List.of(new Forest()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Fires of Victory");
    }

    @Test
    void dealsDamageToPlaneswalkerAndUsesOnlyControllerHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JayaFieryNegotiator());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new FiresOfVictory(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void countsHandAtResolutionRatherThanAtCasting() {
        Permanent target = addCreatureReady(player1, new MoltenMonstrosity());
        harness.setHand(player1, List.of(new FiresOfVictory(), new Forest()));
        addBaseMana();
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void kickedSpellDoesNotDrawWhenItsOnlyTargetLeavesBattlefield() {
        Permanent target = addCreatureReady(player2, new MoltenMonstrosity());
        harness.setHand(player1, List.of(new FiresOfVictory()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new ExtinguishTheLight()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Molten Monstrosity");
        harness.assertInGraveyard(player1, "Fires of Victory");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
