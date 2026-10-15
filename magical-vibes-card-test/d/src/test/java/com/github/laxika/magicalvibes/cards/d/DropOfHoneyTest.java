package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DropOfHoney.class, GrizzlyBears.class, HillGiant.class, MishrasFactory.class})
class DropOfHoneyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the creature with the least power at the controller's upkeep")
    void destroysCreatureWithLeastPower() {
        Permanent leastPower = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent larger = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new DropOfHoney());

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
        harness.addToBattlefield(player1, new DropOfHoney());

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
        harness.addToBattlefield(player1, new DropOfHoney());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Sacrifices itself when there are no creatures on the battlefield")
    void sacrificesItselfWhenNoCreaturesRemain() {
        harness.addToBattlefield(player1, new DropOfHoney());
        harness.runStateBasedActions();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drop of Honey");
        harness.assertInGraveyard(player1, "Drop of Honey");
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DropOfHoney());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void sacrificesAfterDestroyingLastCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DropOfHoney());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drop of Honey");
    }

    @Test
    void sacrificeTriggerDoesNotRecheckCreatureAbsence() {
        harness.addToBattlefield(player1, new DropOfHoney());
        harness.runStateBasedActions();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drop of Honey");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void animatedLandCountsAsCreatureForSacrificeCondition() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, factory)).isTrue();
        harness.addToBattlefield(player1, new DropOfHoney());

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Drop of Honey");
    }

    @Test
    void excludesIndestructibleCreaturesFromMixedTie() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        protectedCreature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DropOfHoney());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first).contains(second);
    }

    @Test
    void doesNotDestroyLargerCreatureWhenLeastPowerIsIndestructible() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        protectedCreature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent larger = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new DropOfHoney());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(larger);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void destroysCreatureWithShroudWithoutTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.getGrantedKeywords().add(Keyword.SHROUD);
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new DropOfHoney());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void usesPowerAtResolutionRatherThanTriggerTime() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new DropOfHoney());

        advanceToUpkeep(player1);
        bears.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(giant);
    }
}
