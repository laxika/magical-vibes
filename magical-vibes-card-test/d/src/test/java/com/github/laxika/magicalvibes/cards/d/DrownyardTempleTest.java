package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Drownyard Temple")
@CardUsed({DrownyardTemple.class})
class DrownyardTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new DrownyardTemple());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns itself from the graveyard to the battlefield tapped")
    void returnsItselfFromGraveyardTapped() {
        DrownyardTemple temple = new DrownyardTemple();
        harness.setGraveyard(player1, List.of(temple));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Drownyard Temple").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Drownyard Temple");
    }

    @Test
    @DisplayName("Can pay the three generic mana cost with colorless mana")
    void paysGraveyardAbilityCost() {
        harness.setGraveyard(player1, List.of(new DrownyardTemple()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the graveyard ability without three mana")
    void cannotActivateWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new DrownyardTemple()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay the graveyard ability's generic cost with colored mana")
    void canPayWithColoredMana() {
        harness.setGraveyard(player1, List.of(new DrownyardTemple()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(findPermanent(player1, "Drownyard Temple").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Drownyard Temple");
    }

    @Test
    @DisplayName("Returns only the activated copy, leaving other Temples in graveyards")
    void returnsOnlyActivatedCopy() {
        DrownyardTemple otherTemple = new DrownyardTemple();
        DrownyardTemple activatedTemple = new DrownyardTemple();
        DrownyardTemple opponentsTemple = new DrownyardTemple();
        harness.setGraveyard(player1, List.of(otherTemple, activatedTemple));
        harness.setGraveyard(player2, List.of(opponentsTemple));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherTemple);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsTemple);
        assertThat(findPermanents(player1, "Drownyard Temple")).hasSize(1);
        assertThat(findPermanent(player1, "Drownyard Temple").getCard()).isSameAs(activatedTemple);
        assertThat(findPermanent(player1, "Drownyard Temple").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Drownyard Temple");
    }

    @Test
    @DisplayName("Can activate twice in response, but the second resolution cannot return it again")
    void repeatedActivationReturnsOnlyOnce() {
        harness.setGraveyard(player1, List.of(new DrownyardTemple()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.assertInGraveyard(player1, "Drownyard Temple");
        harness.assertNotOnBattlefield(player1, "Drownyard Temple");
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Drownyard Temple")).hasSize(1);
        assertThat(findPermanent(player1, "Drownyard Temple").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Drownyard Temple");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
