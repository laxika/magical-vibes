package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.z.ZarielArchdukeOfAvernus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinionOfTheMighty.class, DragonWhelp.class, GrizzlyBears.class, ZarielArchdukeOfAvernus.class})
class MinionOfTheMightyTest extends BaseCardTest {

    @Test
    @DisplayName("Pack tactics puts a Dragon from hand onto the battlefield tapped and attacking")
    void putsDragonTappedAndAttacking() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonWhelp()));

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent dragon = findPermanent(player1, "Dragon Whelp");
        assertThat(dragon.isTapped()).isTrue();
        assertThat(dragon.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Pack tactics offers only Dragon creature cards")
    void offersOnlyDragonCreatures() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new DragonWhelp()));

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Pack tactics does not trigger below total attacking power six")
    void doesNotTriggerBelowThreshold() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonWhelp()));

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The Dragon's attack destination can be chosen independently of Minion")
    void offersIndependentAttackDestination() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ZarielArchdukeOfAvernus());
        harness.setHand(player1, List.of(new DragonWhelp()));

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleCardChosen(player1, 0));

        assertThat(gd.interaction.isAwaitingInput())
                .as("The controller must be offered a choice between the defending player and Zariel")
                .isTrue();
    }

    @Test
    @DisplayName("The controller may decline to put a Dragon onto the battlefield")
    void mayDeclineDragon() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonWhelp()));

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Pack tactics resolves even after an attacking creature leaves the battlefield")
    void retainsPowerQualificationAfterAttackerLeaves() {
        addCreatureReady(player1, new MinionOfTheMighty());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonWhelp()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1, 2, 3)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerGraveyards.get(player1.getId()).add(bear.getCard());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Other attackers do not trigger a Minion that stays back")
    void minionMustAttack() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonWhelp()));

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Pack tactics finishes without a choice when the hand contains no Dragon")
    void noDragonInHand() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
