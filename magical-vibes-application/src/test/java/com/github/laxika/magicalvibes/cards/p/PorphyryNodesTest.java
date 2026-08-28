package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PorphyryNodes.class, GrizzlyBears.class, HillGiant.class})
class PorphyryNodesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the creature with the least power at the controller's upkeep")
    void destroysCreatureWithLeastPower() {
        Permanent leastPower = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent larger = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new PorphyryNodes());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(leastPower);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(larger);
    }

    @Test
    @DisplayName("The controller chooses among creatures tied for least power")
    void controllerChoosesAmongTiedCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent larger = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new PorphyryNodes());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(second).contains(larger);
    }

    @Test
    @DisplayName("The destroyed creature cannot be regenerated")
    void destroyedCreatureCannotBeRegenerated() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setRegenerationShield(1);
        harness.addToBattlefield(player1, new PorphyryNodes());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Sacrifices itself when there are no creatures on the battlefield")
    void sacrificesItselfWhenNoCreaturesRemain() {
        harness.addToBattlefield(player1, new PorphyryNodes());
        harness.runStateBasedActions();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Porphyry Nodes");
        harness.assertInGraveyard(player1, "Porphyry Nodes");
    }
}
