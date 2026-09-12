package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Acridian.class)
class AcridianTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Acridian at its next upkeep")
    void decliningEchoSacrificesAcridian() {
        castAndResolveAcridian();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Acridian");
        harness.assertInGraveyard(player1, "Acridian");
    }

    @Test
    @DisplayName("Paying echo keeps Acridian and echo does not trigger again")
    void payingEchoKeepsAcridianAndIsOneShot() {
        castAndResolveAcridian();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Acridian");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acridian");
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveAcridian();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acridian");
    }

    private void castAndResolveAcridian() {
        harness.castFromHand(player1, new Acridian(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Acridian");
    }
}
