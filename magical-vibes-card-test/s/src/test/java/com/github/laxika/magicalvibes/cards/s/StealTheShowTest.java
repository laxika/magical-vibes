package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.c.ChandraPyromaster;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StealTheShow.class, Shock.class, Divination.class, GrizzlyBears.class,
        ChandraPyromaster.class, ObstinateBaloth.class})
class StealTheShowTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards any number, then draws that many")
    void targetPlayerDiscardsAnyNumberThenDrawsThatMany() {
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Shock(), new Divination()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(player2.getId()), null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Divination");
    }

    @Test
    @DisplayName("Damage mode counts instant and sorcery cards in your graveyard")
    void damageModeCountsInstantAndSorceryCardsInYourGraveyard() {
        harness.setGraveyard(player1, List.of(
                new Shock(), new Divination(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        java.util.UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(targetId), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both modes resolve with their separate targets")
    void bothModesResolveWithSeparateTargets() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        java.util.UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creatureId), null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Shock");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void targetedPlayerMayDiscardZeroCards() {
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player2, List.of(new Divination()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(player2.getId()), null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Shock");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Steal the Show");
    }

    @Test
    void selfDiscardDoesNotApplyOpponentDiscardReplacement() {
        harness.setHand(player1, List.of(new StealTheShow(), new ObstinateBaloth()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(player1.getId()), null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Obstinate Baloth");
        harness.assertNotOnBattlefield(player1, "Obstinate Baloth");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    void damageCountsCardsDiscardedByFirstModeBeforeSpellEntersGraveyard() {
        harness.setHand(player1, List.of(new StealTheShow(), new Shock(), new Divination()));
        harness.setLibrary(player1, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player2, new ChandraPyromaster());
        var target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player1.getId(), target.getId()), null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Steal the Show");
    }

    @Test
    void damageCountsOnlyControllersInstantAndSorceryCards() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player2, new ChandraPyromaster());
        var target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(target.getId()), null);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void damageIsZeroWithNoInstantOrSorceryCardsInGraveyard() {
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        var target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(target.getId()), null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }
}
