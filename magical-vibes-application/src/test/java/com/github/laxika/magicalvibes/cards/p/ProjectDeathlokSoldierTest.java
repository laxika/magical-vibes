package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ProjectDeathlokSoldier.class)
class ProjectDeathlokSoldierTest extends BaseCardTest {

    @Test
    void activatingGraveyardAbilityUsesStackAndPaysMana() {
        ProjectDeathlokSoldier soldier = new ProjectDeathlokSoldier();
        harness.setGraveyard(player1, List.of(soldier));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(soldier.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void resolvingGraveyardAbilityReturnsThisCardToHand() {
        ProjectDeathlokSoldier soldier = new ProjectDeathlokSoldier();
        harness.setGraveyard(player1, List.of(soldier));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(soldier.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(soldier.getId()));
    }

    @Test
    void cannotActivateGraveyardAbilityWithoutEnoughMana() {
        ProjectDeathlokSoldier soldier = new ProjectDeathlokSoldier();
        harness.setGraveyard(player1, List.of(soldier));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsOnlyTheActivatingCopy() {
        ProjectDeathlokSoldier soldier = new ProjectDeathlokSoldier();
        ProjectDeathlokSoldier otherCopy = new ProjectDeathlokSoldier();
        ProjectDeathlokSoldier opponentsCopy = new ProjectDeathlokSoldier();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(otherCopy, soldier));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(soldier);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
    }

    @Test
    void canActivateTwiceButReturnsTheCardOnlyOnce() {
        ProjectDeathlokSoldier soldier = new ProjectDeathlokSoldier();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(soldier));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(soldier);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotPayTheBlackRequirementWithColorlessMana() {
        ProjectDeathlokSoldier soldier = new ProjectDeathlokSoldier();
        harness.setGraveyard(player1, List.of(soldier));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soldier);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void olderActivationCannotReturnCardThatReenteredTheGraveyard() {
        ProjectDeathlokSoldier soldier = new ProjectDeathlokSoldier();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(soldier));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(soldier);
        assertThat(gd.stack).hasSize(1);

        gd.playerHands.get(player1.getId()).remove(soldier);
        harness.setGraveyard(player1, List.of(soldier));
        gd.markGraveyardEntry(soldier);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soldier);
    }
}
