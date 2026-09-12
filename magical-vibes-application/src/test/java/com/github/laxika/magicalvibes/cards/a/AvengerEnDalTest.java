package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SilkenfistOrder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvengerEnDal.class, SilkenfistOrder.class, AccumulatedKnowledge.class})
class AvengerEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Activating starts a discard-cost choice")
    void activationStartsDiscardChoice() {
        addReadyAvenger(player1);
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
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
        Permanent avenger = addReadyAvenger(player1);
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(avenger.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 5);
        harness.assertNotOnBattlefield(player2, "Silkenfist Order");
        harness.assertNotInGraveyard(player2, "Silkenfist Order");
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .contains("Silkenfist Order");
        harness.assertInGraveyard(player1, "Accumulated Knowledge");
    }

    @Test
    @DisplayName("Uses the target's effective toughness before exiling it")
    void gainsLifeUsingEffectiveToughnessBeforeExile() {
        addReadyAvenger(player1);
        Permanent attacker = addAttacker(player2);
        attacker.setToughnessModifier(2);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player2, "Silkenfist Order");
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .contains("Silkenfist Order");
    }

    @Test
    @DisplayName("Pays the costs but fizzles when the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent avenger = addReadyAvenger(player1);
        Permanent attacker = addAttacker(player2);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        harness.passBothPriorities();

        assertThat(avenger.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .doesNotContain("Silkenfist Order");
        harness.assertInGraveyard(player1, "Accumulated Knowledge");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addReadyAvenger(player1);
        Permanent creature = addCreatureReady(player2, new SilkenfistOrder());
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
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
        Permanent attacker = addCreatureReady(player, new SilkenfistOrder());
        int attackerIndex = gd.playerBattlefields.get(player.getId()).indexOf(attacker);
        declareAttackers(player, List.of(attackerIndex));
        return attacker;
    }
}
