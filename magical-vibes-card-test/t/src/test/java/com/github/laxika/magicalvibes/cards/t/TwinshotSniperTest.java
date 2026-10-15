package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoonsnarePrototype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwinshotSniper.class, GrizzlyBears.class, TheWanderingEmperor.class, MoonsnarePrototype.class})
class TwinshotSniperTest extends BaseCardTest {

    @Test
    void entersAndDealsTwoDamageToAnyTargetPlayer() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.setLife(player2, 20);
        addCastingMana();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void entersAndDealsTwoDamageToAnyTargetCreature() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addCastingMana();

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void channelDealsTwoDamageToAnyTargetPlayerAndDiscardsSource() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.setLife(player2, 20);
        addChannelMana();

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Twinshot Sniper");
    }

    @Test
    void channelDealsTwoDamageToAnyTargetCreature() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addChannelMana();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Twinshot Sniper");
    }

    @Test
    void enterTriggerDealsTwoDamageToPlaneswalker() {
        var emperor = harness.addToBattlefieldAndReturn(player2, new TheWanderingEmperor());
        emperor.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new TwinshotSniper()));
        addCastingMana();

        harness.castCreature(player1, 0, emperor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "The Wandering Emperor");
        harness.assertOnBattlefield(player1, "Twinshot Sniper");
    }

    @Test
    void channelDealsTwoDamageToPlaneswalker() {
        var emperor = harness.addToBattlefieldAndReturn(player2, new TheWanderingEmperor());
        emperor.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new TwinshotSniper()));
        addChannelMana();

        harness.activateHandAbility(player1, 0, emperor.getId());
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Twinshot Sniper");
    }

    @Test
    void channelDiscardsAsCostBeforeDamageResolves() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.setLife(player2, 20);
        addChannelMana();

        harness.activateHandAbility(player1, 0, player2.getId());

        harness.assertNotInHand(player1, "Twinshot Sniper");
        harness.assertInGraveyard(player1, "Twinshot Sniper");
        harness.assertNotOnBattlefield(player1, "Twinshot Sniper");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void channelRequiresBothManaOfItsCost() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Twinshot Sniper");
        harness.assertNotInGraveyard(player1, "Twinshot Sniper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelRequiresRedMana() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Twinshot Sniper");
        harness.assertNotInGraveyard(player1, "Twinshot Sniper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelCannotTargetNoncreatureArtifact() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());
        harness.setHand(player1, List.of(new TwinshotSniper()));
        addChannelMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Twinshot Sniper");
        harness.assertNotInGraveyard(player1, "Twinshot Sniper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelCanBeActivatedDuringOpponentsUpkeepAndTargetController() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.setLife(player1, 20);
        addChannelMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateHandAbility(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Twinshot Sniper");
    }

    @Test
    void enterTriggerStillDealsDamageAfterSourceDies() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.setHand(player2, List.of(new TwinshotSniper(), new TwinshotSniper()));
        harness.setLife(player2, 20);
        addCastingMana();
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        var sourceId = harness.getPermanentId(player1, "Twinshot Sniper");

        harness.activateHandAbility(player2, 0, sourceId);
        harness.activateHandAbility(player2, 0, sourceId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Twinshot Sniper");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void channelDoesNotResolveAgainstTargetThatHasLeftBattlefield() {
        var emperor = harness.addToBattlefieldAndReturn(player2, new TheWanderingEmperor());
        emperor.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.setHand(player2, List.of(new TwinshotSniper()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addChannelMana();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, emperor.getId());
        harness.activateHandAbility(player2, 0, emperor.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "The Wandering Emperor");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Twinshot Sniper");
        harness.assertInGraveyard(player2, "Twinshot Sniper");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addChannelMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
