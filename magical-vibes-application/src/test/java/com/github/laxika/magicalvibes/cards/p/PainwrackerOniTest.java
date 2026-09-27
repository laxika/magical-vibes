package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BloodthirstyOgre;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainwrackerOni.class, BloodthirstyOgre.class, HumbleBudoka.class})
class PainwrackerOniTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep: without an Ogre, controller sacrifices a creature")
    void upkeepSacrificesWithoutOgre() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, budoka.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(budoka.getId()));
        harness.assertOnBattlefield(player1, "Painwracker Oni");
    }

    @Test
    @DisplayName("Upkeep: it sacrifices itself when it is the only creature")
    void upkeepSacrificesItselfWhenAlone() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Painwracker Oni");
        harness.assertInGraveyard(player1, "Painwracker Oni");
    }

    @Test
    @DisplayName("Upkeep: no sacrifice while controlling an Ogre")
    void upkeepNoSacrificeWithOgre() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        harness.addToBattlefield(player1, new BloodthirstyOgre());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Painwracker Oni");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(budoka.getId()));
    }

    @Test
    @DisplayName("An opponent's Ogre does not stop the sacrifice")
    void opponentOgreDoesNotHelp() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        harness.addToBattlefield(player2, new BloodthirstyOgre());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Painwracker Oni");
        harness.assertInGraveyard(player1, "Painwracker Oni");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        harness.addToBattlefield(player1, new HumbleBudoka());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Painwracker Oni");
        harness.assertOnBattlefield(player1, "Humble Budoka");
    }

    @Test
    @DisplayName("Upkeep: checks the Ogre condition when the trigger resolves")
    void checksOgreConditionAtResolution() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BloodthirstyOgre());
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ogre));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, budoka.getId());

        harness.assertInGraveyard(player1, "Humble Budoka");
    }

    @Test
    @DisplayName("Upkeep: does not sacrifice when an Ogre enters before resolution")
    void noSacrificeWhenOgreEntersBeforeResolution() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new BloodthirstyOgre());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(budoka.getId()));
    }
}
