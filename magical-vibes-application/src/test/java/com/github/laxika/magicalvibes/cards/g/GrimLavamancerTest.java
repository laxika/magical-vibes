package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Accelerate;
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

@CardUsed({GrimLavamancer.class, Gloomdrifter.class, Accelerate.class})
class GrimLavamancerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to target player and exiles two graveyard cards")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Accelerate(), new Gloomdrifter(), new Accelerate()));
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
        harness.setGraveyard(player1, List.of(new Gloomdrifter(), new Gloomdrifter()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature, killing a 2/2")
    void deals2DamageKillingGloomdrifter() {
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Gloomdrifter(), new Gloomdrifter()));
        harness.addToBattlefield(player2, new Gloomdrifter());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Gloomdrifter");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gloomdrifter");
        harness.assertInGraveyard(player2, "Gloomdrifter");
    }

    @Test
    @DisplayName("Cannot activate with fewer than two cards in the graveyard")
    void cannotActivateWithoutTwoGraveyardCards() {
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Gloomdrifter()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the {R} mana")
    void cannotActivateWithoutMana() {
        addReadyLavamancer(player1);
        harness.setGraveyard(player1, List.of(new Gloomdrifter(), new Gloomdrifter()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent lavamancer = addReadyLavamancer(player1);
        lavamancer.tap();
        harness.setGraveyard(player1, List.of(new Gloomdrifter(), new Gloomdrifter()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyLavamancer(Player player) {
        Permanent perm = addCreatureReady(player, new GrimLavamancer());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
