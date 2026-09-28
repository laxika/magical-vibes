package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
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

@CardUsed({PatronOfTheVein.class, GrizzlyBears.class, Shock.class, VampireNoble.class})
class PatronOfTheVeinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and destroys target creature an opponent controls")
    void entersAndDestroysTargetOpponentCreature() {
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PatronOfTheVein()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0, victim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its own controller")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PatronOfTheVein()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Exiles a dying opponent creature and puts counters on each Vampire controlled")
    void exilesDyingOpponentCreatureAndCountersVampires() {
        Permanent patron = addCreatureReady(player1, new PatronOfTheVein());
        Permanent vampire = addCreatureReady(player1, new VampireNoble());
        Permanent nonVampire = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(victim.getCard());
        assertThat(patron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
