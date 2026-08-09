package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvengerEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Activating starts a discard-cost choice")
    void activationStartsDiscardChoice() {
        addReadyAvenger(player1);
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles an attacking creature and its controller gains life equal to its toughness")
    void exilesAttackerAndGivesItsControllerLife() {
        addReadyAvenger(player1);
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .contains("Grizzly Bears");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addReadyAvenger(player1);
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutDiscardCard() {
        addReadyAvenger(player1);
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAvenger(Player player) {
        return addCreatureReady(player, new AvengerEnDal());
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }
}
