package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JarlOfTheForsakenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a damaged creature an opponent controls")
    void etbDestroysDamagedOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB destroys a damaged planeswalker an opponent controls")
    void etbDestroysDamagedOpponentPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 3);
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Cannot target an opponent permanent that was not dealt damage this turn")
    void cannotTargetUndamagedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Can be foretold and cast from exile later")
    void foretellsAndCastsFromExile() {
        JarlOfTheForsaken jarl = new JarlOfTheForsaken();
        harness.setHand(player1, List.of(jarl));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(jarl.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, jarl.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jarl of the Forsaken");
    }
}
