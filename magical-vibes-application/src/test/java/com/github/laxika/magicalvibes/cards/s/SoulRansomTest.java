package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulRansomTest extends BaseCardTest {

    /** Casts Soul Ransom from player1 onto a fresh player2 creature and resolves it. */
    private Permanent stealCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulRansom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }

    /** Index of the Soul Ransom permanent on its controller's battlefield. */
    private int auraIndex() {
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        return battlefield.indexOf(findPermanent(player1, "Soul Ransom"));
    }

    @Test
    @DisplayName("Soul Ransom steals the enchanted creature")
    void resolvingStealsCreature() {
        Permanent creature = stealCreature();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("An opponent discards two cards: the Aura's controller sacrifices it and draws two")
    void opponentRansomsTheCreatureBack() {
        Permanent creature = stealCreature();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears(), new Mountain()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));

        harness.activateAbility(player2, auraIndex(), 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soul Ransom");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The Aura's controller may not activate the ransom ability")
    void auraControllerCannotActivate() {
        stealCreature();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIndex(), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only your opponents");
    }

    @Test
    @DisplayName("An opponent with fewer than two cards in hand cannot activate the ability")
    void cannotActivateWithoutTwoCards() {
        stealCreature();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player2, auraIndex(), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
