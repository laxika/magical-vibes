package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FatalBlow;
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

@CardUsed({ErtaisFamiliar.class, FatalBlow.class})
class ErtaisFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Phasing out during the controller's untap step mills three cards")
    void phasesOutAndMillsThree() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new ErtaisFamiliar());
        stockLibrary(player1);

        advanceToUpkeep(player2);
        advanceToUpkeep(player1); // the Familiar phases out during the untap step

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(familiar);

        harness.passBothPriorities(); // resolve the phase-out trigger

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Leaving the battlefield mills three cards for the Familiar's controller")
    void leavesBattlefieldAndMillsThreeForController() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player2, new ErtaisFamiliar());
        stockLibrary(player2);

        gd.permanentsDealtDamageThisTurn.add(familiar.getId());
        harness.setHand(player1, List.of(new FatalBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, familiar.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ertai's Familiar");

        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        // The Familiar itself plus the three milled Fatal Blows.
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertInGraveyard(player2, "Ertai's Familiar");
    }

    @Test
    @DisplayName("The {U} ability stops the Familiar from phasing out at the next untap step")
    void abilityPreventsPhaseOut() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new ErtaisFamiliar());
        stockLibrary(player1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        advanceToUpkeep(player1); // the Familiar stays put, so nothing is milled

        harness.assertOnBattlefield(player1, "Ertai's Familiar");
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(familiar);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The {U} ability requires blue mana")
    void abilityRequiresBlueMana() {
        harness.addToBattlefield(player1, new ErtaisFamiliar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The {U} ability expires at the controller's next upkeep")
    void abilityExpiresAtNextUpkeep() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new ErtaisFamiliar());
        stockLibrary(player1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        advanceToUpkeep(player1); // the restriction still applies during this untap step
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(familiar);

        advanceToUpkeep(player2);
        advanceToUpkeep(player1); // the restriction expired at the preceding upkeep
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(familiar);

        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private void stockLibrary(Player player) {
        harness.setHand(player, List.of());
        harness.setLibrary(player, List.of(new FatalBlow(), new FatalBlow(), new FatalBlow(), new FatalBlow(),
                new FatalBlow(), new FatalBlow(), new FatalBlow(), new FatalBlow()));
    }
}
