package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpassionedOrator.class, GreenwoodSentinel.class, RaiseTheAlarm.class})
class ImpassionedOratorTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when it enters")
    void noLifeOnOwnEnter() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ImpassionedOrator()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's creature enters")
    void noLifeOnOpponentCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.enterBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each creature token entering creates a separate life gain trigger")
    void gainsLifeForEachToken() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An existing Orator triggers when another Orator enters, but the entering one does not")
    void secondOratorTriggersOnlyExistingOrator() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.setHand(player1, List.of(new ImpassionedOrator()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Multiple Orators each trigger for an allied creature")
    void multipleOratorsEachGainLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }
}
