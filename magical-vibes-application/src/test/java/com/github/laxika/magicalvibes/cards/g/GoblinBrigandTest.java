package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinBrigand.class, Humility.class, Pacifism.class})
class GoblinBrigandTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring Goblin Brigand as attacker succeeds")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new GoblinBrigand());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declaring no attackers when Goblin Brigand can attack throws exception")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new GoblinBrigand());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Goblin Brigand does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        Permanent brigand = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());

        declareAttackers(List.of());

        assertThat(brigand.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Goblin Brigand does not need to attack when tapped")
    void doesNotAttackWhenTapped() {
        Permanent brigand = addCreatureReady(player1, new GoblinBrigand());
        brigand.tap();

        declareAttackers(List.of());

        assertThat(brigand.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Goblin Brigand does not need to attack after losing its abilities")
    void doesNotAttackAfterLosingAbilities() {
        harness.addToBattlefield(player2, new Humility());
        Permanent brigand = addCreatureReady(player1, new GoblinBrigand());

        declareAttackers(List.of());

        assertThat(brigand.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Every able Goblin Brigand must attack")
    void cannotOmitOneOfMultipleBrigands() {
        addCreatureReady(player1, new GoblinBrigand());
        addCreatureReady(player1, new GoblinBrigand());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("An opposing Goblin Brigand does not force the active player to attack")
    void opposingBrigandDoesNotForceAttack() {
        Permanent brigand = addCreatureReady(player2, new GoblinBrigand());

        declareAttackers(List.of());

        assertThat(brigand.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Pacifism prevents Goblin Brigand from attacking despite its requirement")
    void attackRestrictionOverridesRequirement() {
        Permanent brigand = addCreatureReady(player1, new GoblinBrigand());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castEnchantment(player2, 0, brigand.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Pacifism").getAttachedTo()).isEqualTo(brigand.getId());

        declareAttackers(List.of());

        assertThat(brigand.isAttacking()).isFalse();
    }
}
