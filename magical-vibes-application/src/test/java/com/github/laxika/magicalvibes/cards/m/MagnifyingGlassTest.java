package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagnifyingGlass.class})
class MagnifyingGlassTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Magnifying Glass adds a colorless mana")
    void tappingAddsColorlessMana() {
        Permanent glass = addReadyGlass();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(glass.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four mana and tapping Magnifying Glass creates a Clue")
    void paysFourManaAndInvestigates() {
        Permanent glass = addReadyGlass();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(glass.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("A newly entered noncreature Magnifying Glass can tap for mana")
    void newlyEnteredGlassCanAddMana() {
        Permanent glass = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(glass.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Investigating uses the stack and creates the Clue only on resolution")
    void investigationWaitsForResolution() {
        harness.addToBattlefield(player1, new MagnifyingGlass());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(gd.playersWhoInvestigatedThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("A tapped Clue can be sacrificed for two mana to draw a card")
    void clueSacrificeDrawsOnResolution() {
        addReadyGlass();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent clue = findPermanent(player1, "Clue");
        clue.setTapped(true);
        MagnifyingGlass drawnCard = new MagnifyingGlass();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A tapped Magnifying Glass cannot activate either tap ability")
    void tappedGlassCannotActivate() {
        Permanent glass = addReadyGlass();
        glass.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private Permanent addReadyGlass() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
