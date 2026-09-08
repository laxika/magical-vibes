package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FlamecoreElemental.class)
class FlamecoreElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Flamecore Elemental at its next upkeep")
    void decliningEchoSacrificesFlamecoreElemental() {
        castAndResolveFlamecoreElemental();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Flamecore Elemental");
        harness.assertInGraveyard(player1, "Flamecore Elemental");
    }

    @Test
    @DisplayName("Paying echo keeps Flamecore Elemental and echo does not trigger again")
    void payingEchoKeepsFlamecoreElementalAndIsOneShot() {
        castAndResolveFlamecoreElemental();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 4);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Flamecore Elemental");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Flamecore Elemental");
    }

    private void castAndResolveFlamecoreElemental() {
        harness.setHand(player1, List.of(new FlamecoreElemental()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Flamecore Elemental");
    }
}
