package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

class HavengulRunebinderTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability: {2}{U}, {T}, exile creature card, create Zombie + counters")
    void hasActivatedAbility() {
        HavengulRunebinder card = new HavengulRunebinder();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}{U}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(3);

        assertThat(ability.getEffects().get(0)).isInstanceOf(ExileCardFromGraveyardCost.class);
        ExileCardFromGraveyardCost exileCost = (ExileCardFromGraveyardCost) ability.getEffects().get(0);
        assertThat(exileCost.requiredType()).isEqualTo(CardType.CREATURE);

        assertThat(ability.getEffects().get(1)).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect = (CreateTokenEffect) ability.getEffects().get(1);
        assertThat(tokenEffect.tokenName()).isEqualTo("Zombie");
        assertThat(tokenEffect.power()).isEqualTo(2);
        assertThat(tokenEffect.toughness()).isEqualTo(2);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.BLACK);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.ZOMBIE);

        assertThat(ability.getEffects().get(2))
                .isInstanceOf(PutCounterOnEachControlledPermanentEffect.class);
    }

    // ===== Activation / cost payment =====

    @Test
    @DisplayName("Activating prompts for graveyard exile cost choice")
    void promptsForGraveyardExileCost() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
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
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(runebinder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ({2}{U})")
    void manaIsConsumed() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        // 4 - 3 ({2}{U}) = 1 mana remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving creates a 2/2 black Zombie token")
    void createsZombieToken() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("The created Zombie token receives a +1/+1 counter (2/2 base + counter)")
    void newTokenGetsCounter() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pre-existing Zombie creatures you control also get a +1/+1 counter")
    void existingZombiesGetCounter() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        Permanent gravedigger = harness.addToBattlefieldAndReturn(player1, new Gravedigger());

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gravedigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Zombie creatures and the Runebinder itself do not get a counter")
    void nonZombiesDoNotGetCounter() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(runebinder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's Zombies do not get a counter")
    void opponentZombiesDoNotGetCounter() {
        Permanent runebinder = harness.addToBattlefieldAndReturn(player1, new HavengulRunebinder());
        runebinder.setSummoningSick(false);
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new Gravedigger());

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(opponentZombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    // ===== Validation =====

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
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
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
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(runebinder);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
