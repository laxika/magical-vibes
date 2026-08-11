package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeraldOfSerraTest extends BaseCardTest {

    @Test
    void decliningEchoSacrificesHerald() {
        castAndResolveHerald();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Herald of Serra");
        harness.assertInGraveyard(player1, "Herald of Serra");
    }

    @Test
    void payingEchoKeepsHeraldAndEchoIsOneShot() {
        castAndResolveHerald();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Herald of Serra");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Herald of Serra");
    }

    @Test
    void echoDoesNotTriggerDuringOpponentUpkeep() {
        castAndResolveHerald();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Herald of Serra");
    }

    private void castAndResolveHerald() {
        harness.setHand(player1, java.util.List.of(new HeraldOfSerra()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
