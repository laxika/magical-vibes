package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraPyromaster;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.m.Megrim;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvacynGuardianAngel.class, RuneclawBear.class, LightningStrike.class, ChandraPyromaster.class,
        Megrim.class, MindRot.class})
class AvacynGuardianAngelTest extends BaseCardTest {

    @Test
    void firstAbilityPreventsDamageFromChosenColorToAnotherCreature() {
        Permanent avacyn = addAvacyn();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        addManaForFirstAbility();

        harness.activateAbility(player1, battlefieldIndex(avacyn), 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void firstAbilityDoesNotPreventDamageFromOtherColors() {
        Permanent avacyn = addAvacyn();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        addManaForFirstAbility();

        harness.activateAbility(player1, battlefieldIndex(avacyn), 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void secondAbilityPreventsDamageToAPlaneswalker() {
        Permanent avacyn = addAvacyn();
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraPyromaster());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        addManaForSecondAbility();

        harness.activateAbility(player1, battlefieldIndex(avacyn), 1, null, chandra.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void secondAbilityPreventsDamageFromDiscardTriggersOfChosenColor() {
        Permanent avacyn = addAvacyn();
        harness.addToBattlefield(player1, new Megrim());
        harness.setLife(player2, 20);
        addManaForSecondAbility();
        harness.activateAbility(player1, battlefieldIndex(avacyn), 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.setHand(player2, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void secondAbilityProtectsOpponentFromMultipleDamageEventsButNotOtherPlayer() {
        Permanent avacyn = addAvacyn();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addManaForSecondAbility();

        harness.activateAbility(player1, battlefieldIndex(avacyn), 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 17);
    }

    @Test
    void secondAbilityDoesNotPreventPlayerDamageFromOtherColors() {
        Permanent avacyn = addAvacyn();
        harness.setLife(player1, 20);
        addManaForSecondAbility();
        harness.activateAbility(player1, battlefieldIndex(avacyn), 1, null, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);
    }

    @Test
    void firstAbilityCannotTargetAvacynItself() {
        Permanent avacyn = addAvacyn();
        addManaForFirstAbility();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(avacyn), 0, null, avacyn.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstAbilityCannotTargetPlayer() {
        Permanent avacyn = addAvacyn();
        addManaForFirstAbility();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(avacyn), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondAbilityCannotTargetOrdinaryCreature() {
        Permanent avacyn = addAvacyn();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        addManaForSecondAbility();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(avacyn), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureProtectionExpiresAtEndOfTurn() {
        Permanent avacyn = addAvacyn();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        addManaForFirstAbility();
        harness.activateAbility(player1, battlefieldIndex(avacyn), 0, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    private Permanent addAvacyn() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new AvacynGuardianAngel());
        avacyn.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return avacyn;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addManaForFirstAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void addManaForSecondAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

}
