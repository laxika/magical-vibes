package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SchoolOfTheUnseen;
import com.github.laxika.magicalvibes.cards.t.ThawingGlaciers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianWarBeast.class, Pyrokinesis.class, SchoolOfTheUnseen.class, ThawingGlaciers.class})
class PhyrexianWarBeastTest extends BaseCardTest {

    /** Deals lethal damage to the War Beast so its leaves-the-battlefield ability triggers. */
    private void killWarBeast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Pyrokinesis()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castInstant(player2, 0, Map.of(harness.getPermanentId(player1, "Phyrexian War Beast"), 4));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Leaving the battlefield sacrifices the sole land and deals 1 damage to its controller")
    void leavesBattlefieldSacrificesLandAndDealsDamage() {
        harness.addToBattlefield(player1, new PhyrexianWarBeast());
        harness.addToBattlefield(player1, new SchoolOfTheUnseen());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        killWarBeast();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "School of the Unseen");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("With several lands the controller chooses which one to sacrifice")
    void controllerChoosesWhichLandToSacrifice() {
        harness.addToBattlefield(player1, new PhyrexianWarBeast());
        harness.addToBattlefield(player1, new SchoolOfTheUnseen());
        harness.addToBattlefield(player1, new ThawingGlaciers());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        killWarBeast();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        Permanent school = findPermanent(player1, "School of the Unseen");
        harness.handleMultiplePermanentsChosen(player1, List.of(school.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "School of the Unseen");
        harness.assertOnBattlefield(player1, "Thawing Glaciers");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Sacrifices a land before dealing damage")
    void sacrificesLandBeforeDealingDamage() {
        harness.addToBattlefield(player1, new PhyrexianWarBeast());
        harness.addToBattlefield(player1, new SchoolOfTheUnseen());
        harness.addToBattlefield(player1, new ThawingGlaciers());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        killWarBeast();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).as("land sacrifice must resolve before the damage instruction").isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("With no lands the controller still takes 1 damage")
    void noLandsStillDealsDamage() {
        harness.addToBattlefield(player1, new PhyrexianWarBeast());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        killWarBeast();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
    }
}
