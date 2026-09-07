package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VotaryOfTheConclave.class, Shock.class})
class VotaryOfTheConclaveTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability grants a regeneration shield")
    void activatingAbilityGrantsRegenerationShield() {
        harness.addToBattlefield(player1, new VotaryOfTheConclave());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Votary of the Conclave").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves it from lethal damage")
    void regenerationShieldSavesFromLethalDamage() {
        harness.addToBattlefield(player1, new VotaryOfTheConclave());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        UUID votaryId = harness.getPermanentId(player1, "Votary of the Conclave");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, votaryId);
        harness.passBothPriorities();

        Permanent votary = findPermanent(player1, "Votary of the Conclave");
        assertThat(votary).isNotNull();
        assertThat(votary.getRegenerationShield()).isZero();
    }
}
