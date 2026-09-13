package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CitanulCentaurs.class, Humble.class})
class CitanulCentaursTest extends BaseCardTest {

    @Test
    @DisplayName("Citanul Centaurs cannot be targeted by spells")
    void shroudPreventsTargeting() {
        var centaurs = harness.addToBattlefieldAndReturn(player1, new CitanulCentaurs());

        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, centaurs.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining echo sacrifices Citanul Centaurs at its next upkeep")
    void decliningEchoSacrificesCentaurs() {
        castAndResolveCentaurs();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Citanul Centaurs");
        harness.assertInGraveyard(player1, "Citanul Centaurs");
    }

    @Test
    @DisplayName("Paying echo keeps Citanul Centaurs and echo does not trigger again")
    void payingEchoKeepsCentaursAndIsOneShot() {
        castAndResolveCentaurs();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Citanul Centaurs");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Citanul Centaurs");
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveCentaurs();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Citanul Centaurs");
    }

    private void castAndResolveCentaurs() {
        harness.castFromHand(player1, new CitanulCentaurs(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Citanul Centaurs");
    }
}
