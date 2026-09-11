package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfSerra.class, Confiscate.class})
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

    @Test
    void echoTriggersAtTheCurrentControllersNextUpkeep() {
        castAndResolveHerald();
        Permanent herald = findPermanent(player1, "Herald of Serra");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, herald.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Herald of Serra");
        harness.assertOnBattlefield(player2, "Herald of Serra");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Herald of Serra");

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Herald of Serra");
        harness.assertInGraveyard(player1, "Herald of Serra");
    }

    private void castAndResolveHerald() {
        harness.castFromHand(player1, new HeraldOfSerra(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
