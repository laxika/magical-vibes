package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LifeAndLimb;
import com.github.laxika.magicalvibes.cards.s.SaltfieldRecluse;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PorphyryNodes.class, SaltfieldRecluse.class, PouncingWurm.class})
class PorphyryNodesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the creature with the least power at the controller's upkeep")
    void destroysCreatureWithLeastPower() {
        Permanent leastPower = harness.addToBattlefieldAndReturn(player2, new SaltfieldRecluse());
        Permanent larger = harness.addToBattlefieldAndReturn(player2, new PouncingWurm());
        harness.addToBattlefield(player1, new PorphyryNodes());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(leastPower);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(larger);
    }

    @Test
    @DisplayName("The controller chooses among creatures tied for least power")
    void controllerChoosesAmongTiedCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SaltfieldRecluse());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SaltfieldRecluse());
        Permanent larger = harness.addToBattlefieldAndReturn(player2, new PouncingWurm());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SaltfieldRecluse());
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

    @Test
    @DisplayName("Sacrifices itself after destroying the last creature")
    void sacrificesItselfAfterDestroyingLastCreature() {
        harness.addToBattlefield(player2, new SaltfieldRecluse());
        harness.addToBattlefield(player1, new PorphyryNodes());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Saltfield Recluse");
        harness.assertNotOnBattlefield(player1, "Porphyry Nodes");
        harness.assertInGraveyard(player1, "Porphyry Nodes");
    }

    @CardUsed({LifeAndLimb.class, Forest.class})
    @Test
    @DisplayName("Does not sacrifice itself while an animated Forest is a creature")
    void remainsOnBattlefieldWhileAnimatedForestIsCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LifeAndLimb());
        harness.addToBattlefield(player1, new PorphyryNodes());

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Forest"))).isTrue();

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Porphyry Nodes");
    }
}
