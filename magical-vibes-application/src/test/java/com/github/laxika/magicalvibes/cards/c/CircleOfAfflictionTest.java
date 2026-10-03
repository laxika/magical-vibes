package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MireBoa;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.p.Pyrohemia;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfAffliction.class, MireBoa.class, ProdigalPyromancer.class, Pyrohemia.class})
class CircleOfAfflictionTest extends BaseCardTest {

    @Test
    void chosenColorSourceDamageCanBePaidToDrainTargetPlayer() {
        addCircleChoosing(CardColor.RED);
        preparePlayer1MainPhase();
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void decliningPaymentDoesNotDrainTargetPlayer() {
        addCircleChoosing(CardColor.RED);
        preparePlayer1MainPhase();
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void damageToAnotherPlayerDoesNotTrigger() {
        addCircleChoosing(CardColor.RED);
        preparePlayer1MainPhase();
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void sourceOfAnotherColorDoesNotTrigger() {
        addCircleChoosing(CardColor.RED);
        preparePlayer1MainPhase();
        Permanent attacker = addCreatureReady(player1, new MireBoa());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void chosenColorCombatDamageAlsoTriggers() {
        addCircleChoosing(CardColor.RED);
        preparePlayer1MainPhase();
        Permanent attacker = addCreatureReady(player1, new ProdigalPyromancer());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void controllerCanTargetThemselvesAfterTheirOwnSourceDealsDamage() {
        addCircleChoosing(CardColor.RED);
        preparePlayer1MainPhase();
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void damageToControllersCreatureDoesNotTrigger() {
        addCircleChoosing(CardColor.RED);
        preparePlayer1MainPhase();
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent victim = addCreatureReady(player2, new MireBoa());

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Mire Boa");
    }

    @Test
    void singleHitOfTwoDamageTriggersOnlyOnceAndGainsOnlyOneLife() {
        addCircleChoosing(CardColor.GREEN);
        preparePlayer1MainPhase();
        Permanent attacker = addCreatureReady(player1, new MireBoa());
        attacker.setAttacking(true);

        resolveCombat();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneousDamageToBothPlayersTriggersOnlyOnceForController() {
        addCircleChoosing(CardColor.RED);
        preparePlayer1MainPhase();
        harness.addToBattlefield(player1, new Pyrohemia());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addCircleChoosing(CardColor color) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CircleOfAffliction()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, color.name());
    }

    private void preparePlayer1MainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
