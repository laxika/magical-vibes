package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoratamiMirrorMage.class, Island.class, WanderingOnes.class})
class SoratamiMirrorMageTest extends BaseCardTest {

    @Test
    @DisplayName("Returns three lands as a cost and bounces the target creature")
    void bouncesTargetCreature() {
        harness.addToBattlefield(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInHand(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Cannot activate with only two lands to return")
    void cannotActivateWithTwoLands() {
        harness.addToBattlefield(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land is an illegal target")
    void rejectsLandTarget() {
        harness.addToBattlefield(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chooses exactly three lands when more are available")
    void choosesExactlyThreeLandsWhenMoreAreAvailable() {
        harness.addToBattlefield(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, target.getId());

        assertThat(gd.stack).isEmpty();
        List<Permanent> islands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Island"))
                .toList();
        harness.handlePermanentChosen(player1, islands.get(0).getId());
        harness.handlePermanentChosen(player1, islands.get(1).getId());
        harness.handlePermanentChosen(player1, islands.get(2).getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Island"))
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Island"))
                .hasSize(3);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInHand(player1, "Soratami Mirror-Mage");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
