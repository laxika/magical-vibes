package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VotaryOfTheConclave.class, Char.class})
class VotaryOfTheConclaveTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability grants a regeneration shield")
    void activatingAbilityGrantsRegenerationShield() {
        Permanent votary = harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(votary.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves it from lethal damage")
    void regenerationShieldSavesFromLethalDamage() {
        Permanent votary = harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, votary.getId());

        harness.assertOnBattlefield(player1, "Votary of the Conclave");
        assertThat(votary.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without the two generic mana")
    void cannotActivateWithoutGenericMana() {
        harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped creature can activate regeneration repeatedly")
    void tappedCreatureCanActivateRepeatedly() {
        Permanent votary = harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave());
        votary.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(votary.getRegenerationShield()).isEqualTo(2);
        assertThat(votary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lethal damage in response kills it before regeneration resolves")
    void lethalDamageInResponseKillsBeforeRegenerationResolves() {
        Permanent votary = harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, votary.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Votary of the Conclave");
        harness.assertInGraveyard(player1, "Votary of the Conclave");
    }
}
