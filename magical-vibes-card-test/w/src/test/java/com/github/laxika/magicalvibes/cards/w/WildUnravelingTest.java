package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildUnraveling.class, GrizzlyBears.class, SerraAngel.class})
class WildUnravelingTest extends BaseCardTest {

    @Test
    void blightsACreatureAsTheAdditionalCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player2, List.of(new WildUnraveling()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantWithSacrifice(player2, 0, bears.getId(), creature.getId());

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Wild Unraveling");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void paysManaInsteadOfBlighting() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new WildUnraveling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player2, 0, bears.getId(), null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Wild Unraveling");
    }

    @Test
    void cannotCastWithoutACreatureOrManaForTheAdditionalCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new WildUnraveling()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player2, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("put counters on a creature you control or pay");
    }

    @Test
    void canBlightACreatureEvenWhenTheCountersKillIt() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        Permanent payment = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new WildUnraveling()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantWithSacrifice(player2, 0, spell.getId(), payment.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Wild Unraveling");
    }

    @Test
    void cannotBlightAnOpponentsCreatureToPayTheCost() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player2, List.of(new WildUnraveling()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player2, 0, spell.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInHand(player2, "Wild Unraveling");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
    }
}
