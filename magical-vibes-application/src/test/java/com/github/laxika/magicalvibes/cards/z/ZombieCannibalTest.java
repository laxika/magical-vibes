package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZombieCannibal.class, ZombieAssassin.class, ZombieInfestation.class})
class ZombieCannibalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may exile one chosen card from the damaged player's graveyard")
    void combatDamageExilesOneChosenCard() {
        ZombieAssassin assassin = new ZombieAssassin();
        ZombieInfestation infestation = new ZombieInfestation();
        harness.setGraveyard(player2, List.of(assassin, infestation));

        attackDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(infestation.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Zombie Assassin");
        harness.assertNotInGraveyard(player2, "Zombie Infestation");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(infestation);
    }

    @Test
    @DisplayName("Declining the optional graveyard exile exiles nothing")
    void decliningExileExilesNothing() {
        ZombieAssassin assassin = new ZombieAssassin();
        harness.setGraveyard(player2, List.of(assassin));

        attackDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Zombie Assassin");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the damaged player's graveyard is offered")
    void controllerGraveyardIsNotOffered() {
        ZombieAssassin ownAssassin = new ZombieAssassin();
        ZombieInfestation theirInfestation = new ZombieInfestation();
        harness.setGraveyard(player1, List.of(ownAssassin));
        harness.setGraveyard(player2, List.of(theirInfestation));

        attackDealingDamage();

        List<UUID> valid = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(valid).containsExactly(theirInfestation.getId());
    }

    @Test
    @DisplayName("Combat damage to a creature does not trigger the graveyard exile")
    void damageToCreatureDoesNotTriggerAbility() {
        ZombieAssassin graveyardCard = new ZombieAssassin();
        harness.setGraveyard(player2, List.of(graveyardCard));

        Permanent attacker = addCreatureReady(player1, new ZombieCannibal());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ZombieAssassin());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Zombie Assassin");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty graveyard presents no choice")
    void emptyGraveyardPresentsNoChoice() {
        harness.setGraveyard(player2, List.of());

        attackDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void attackDealingDamage() {
        Permanent zombieCannibal = addCreatureReady(player1, new ZombieCannibal());
        zombieCannibal.setAttacking(true);
        resolveCombat();
    }
}
