package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimLavamancer.class, RuneclawBear.class, Shock.class, ChandraTheFirebrand.class})
class GrimLavamancerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to target player and exiles two graveyard cards")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new RuneclawBear(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleMultipleCardsChosen(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .limit(2)
                .map(Card::getId)
                .toList());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Ability can deal 2 damage to its controller")
    void deals2DamageToController() {
        harness.setLife(player1, 20);
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature, killing a 2/2")
    void deals2DamageKillingRuneclawBear() {
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Cannot activate with fewer than two cards in the graveyard")
    void cannotActivateWithoutTwoGraveyardCards() {
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the {R} mana")
    void cannotActivateWithoutMana() {
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new RuneclawBear(), new RuneclawBear()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent lavamancer = addReadyLavamancer(player1);
        lavamancer.tap();
        harness.setGraveyard(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void paysCostsBeforeDamageResolves() {
        Permanent lavamancer = addReadyLavamancer(player1);
        Shock first = new Shock();
        RuneclawBear second = new RuneclawBear();
        Shock remaining = new Shock();
        harness.setGraveyard(player1, List.of(first, remaining, second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(lavamancer.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotPayWithOpponentsGraveyard() {
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Shock(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent lavamancer = addReadyLavamancer(player1);
        lavamancer.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new Shock(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void abilityResolvesAfterSourceDies() {
        Permanent lavamancer = addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, lavamancer.getId());
        harness.assertNotOnBattlefield(player1, "Grim Lavamancer");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void dealsDamageToPlaneswalker() {
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new RuneclawBear()));
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        chandra.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void costsRemainPaidWhenTargetDiesBeforeResolution() {
        Permanent lavamancer = addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new RuneclawBear()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(lavamancer.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    private Permanent addReadyLavamancer(Player player) {
        Permanent perm = addCreatureReady(player, new GrimLavamancer());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
