package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JennyFlint.class, Clue.class, Food.class, GrizzlyBears.class})
class JennyFlintTest extends BaseCardTest {

    @Test
    void sacrificingCluePutsCounterOnAnotherCreatureYouControl() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent clue = harness.addToBattlefieldAndReturn(player1, new Clue());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sacrificingFoodPutsCounterOnAnotherCreatureYouControl() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new Food());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void trainingPutsCounterOnJennyWhenSheAttacksWithALargerCreature() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        Permanent giant = addCreatureReady(player1, new GrizzlyBears());
        giant.setPowerModifier(1);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void partnerWithLetsTheTargetPlayerSearchForMadameVastra() {
        Card vastra = new Card();
        vastra.setName("Madame Vastra");
        harness.setLibrary(player2, List.of(vastra));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new JennyFlint());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(vastra);
    }
}
