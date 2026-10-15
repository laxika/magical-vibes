package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.FalkenrathTorturer;
import com.github.laxika.magicalvibes.cards.c.CurseOfThirst;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HavengulRunebinder.class, FalkenrathTorturer.class, HeadlessSkaab.class, CurseOfThirst.class})
class HavengulRunebinderTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate a tapped Runebinder")
    void cannotActivateWhenTapped() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        runebinder.tap();
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating prompts for graveyard exile cost choice")
    void promptsForGraveyardExileCost() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Creature card is exiled from graveyard and source is tapped as cost")
    void exilesCreatureAndTapsSource() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Falkenrath Torturer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Falkenrath Torturer"));
        assertThat(runebinder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ({2}{U})")
    void manaIsConsumed() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        // 4 - 3 ({2}{U}) = 1 mana remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving creates a black Zombie creature token")
    void createsZombieToken() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("The created Zombie token receives a +1/+1 counter (2/2 base + counter)")
    void newTokenGetsCounter() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pre-existing Zombie creatures you control also get a +1/+1 counter")
    void existingZombiesGetCounter() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        Permanent existingZombie = harness.addToBattlefieldAndReturn(player1, new HeadlessSkaab());

        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(existingZombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Zombie creatures and the Runebinder itself do not get a counter")
    void nonZombiesDoNotGetCounter() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        Permanent nonZombie = harness.addToBattlefieldAndReturn(player1, new FalkenrathTorturer());

        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(nonZombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(runebinder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's Zombies do not get a counter")
    void opponentZombiesDoNotGetCounter() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());

        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(opponentZombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate without a creature card in graveyard")
    void cannotActivateWithoutCreatureInGraveyard() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        // summoning sick by default
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature card cannot pay the graveyard cost")
    void cannotExileNoncreatureCard() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new CurseOfThirst()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        harness.assertInGraveyard(player1, "Curse of Thirst");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature in the opponent's graveyard cannot pay the cost")
    void cannotExileOpponentsCreature() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        harness.assertInGraveyard(player2, "Falkenrath Torturer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zombies are determined at resolution even if the Runebinder has left")
    void countersUseBattlefieldAtResolution() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new FalkenrathTorturer()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        gd.playerBattlefields.get(player1.getId()).remove(runebinder);
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new HeadlessSkaab());
        harness.passBothPriorities();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }
}
