package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PainwrackerOniTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep: without an Ogre, controller sacrifices a creature")
    void upkeepSacrificesWithoutOgre() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertOnBattlefield(player1, "Painwracker Oni");
    }

    @Test
    @DisplayName("Upkeep: it may sacrifice itself when it is the only creature")
    void upkeepCanSacrificeItself() {
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrayOgre());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Painwracker Oni");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("An opponent's Ogre does not stop the sacrifice")
    void opponentOgreDoesNotHelp() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        harness.addToBattlefield(player2, new GrayOgre());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Painwracker Oni");
        harness.assertInGraveyard(player1, "Painwracker Oni");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new PainwrackerOni());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Painwracker Oni");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
