package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenPriest.class, GreenwoodSentinel.class, Manalith.class, Murder.class})
class DwarvenPriestTest extends BaseCardTest {

    private void castPriest() {
        harness.setHand(player1, List.of(new DwarvenPriest()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Entering gains 1 life for each creature you control, counting itself")
    void gainsLifePerCreature() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setLife(player1, 20);

        castPriest();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Opponent's creatures are not counted")
    void ignoresOpponentCreatures() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);

        castPriest();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Noncreature permanents do not increase the life gained")
    void ignoresNoncreaturePermanents() {
        harness.addToBattlefield(player1, new Manalith());
        harness.setLife(player1, 20);

        castPriest();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves are counted")
    void countsCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DwarvenPriest()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new GreenwoodSentinel());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The trigger survives the Priest's removal and counts only remaining creatures")
    void resolvesAfterPriestIsDestroyed() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DwarvenPriest()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Dwarven Priest"));
        harness.assertNotOnBattlefield(player1, "Dwarven Priest");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
