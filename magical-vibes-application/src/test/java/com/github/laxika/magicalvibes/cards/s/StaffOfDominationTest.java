package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StaffOfDomination.class, DrossCrocodile.class})
class StaffOfDominationTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps itself for one mana")
    void untapsItself() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        staff.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gains one life")
    void gainsLife() {
        harness.addToBattlefield(player1, new StaffOfDomination());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Tap-cost abilities tap Staff of Domination")
    void tapCostAbilityTapsStaff() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a target creature")
    void untapsTargetCreature() {
        harness.addToBattlefield(player1, new StaffOfDomination());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps a target creature")
    void tapsTargetCreature() {
        harness.addToBattlefield(player1, new StaffOfDomination());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 3, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws a card")
    void drawsCard() {
        harness.addToBattlefield(player1, new StaffOfDomination());
        harness.setLibrary(player1, List.of(new DrossCrocodile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, staff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Untapping the Staff allows another life-gain activation")
    void canReuseStaffAfterUntapping() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(staff.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(staff.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(staff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Staff cannot pay a tap activation cost")
    void tappedStaffCannotGainLife() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        staff.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain still resolves after the Staff leaves the battlefield")
    void lifeGainResolvesWithoutSource() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(staff);
        harness.setGraveyard(player1, List.of(staff.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Self-untap affects only the Staff that activated it")
    void selfUntapDoesNotUntapAnotherStaff() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        Permanent otherStaff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        staff.tap();
        otherStaff.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isFalse();
        assertThat(otherStaff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability also rejects noncreature targets")
    void tapAbilityRejectsNonCreatureTarget() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, staff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
