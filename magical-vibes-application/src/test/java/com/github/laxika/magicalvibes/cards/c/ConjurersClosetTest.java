package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ControlMagic.class, ConjurersCloset.class, Vorstclaw.class})
class ConjurersClosetTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a creature you control at your end step")
    void flickersOwnCreatureAtYourEndStep() {
        harness.addToBattlefield(player1, new ConjurersCloset());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new Vorstclaw()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Vorstclaw");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Vorstclaw"));

        Permanent returned = findPermanent(player1, "Vorstclaw");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Declining the may ability leaves the creature untouched")
    void decliningLeavesCreatureAlone() {
        harness.addToBattlefield(player1, new ConjurersCloset());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new Vorstclaw()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new ConjurersCloset());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new Vorstclaw()).getId();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Does not trigger when only an opponent controls a creature")
    void doesNotTriggerWithOnlyOpponentCreatures() {
        harness.addToBattlefield(player1, new ConjurersCloset());
        harness.addToBattlefield(player2, new Vorstclaw());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Vorstclaw");
    }

    @Test
    @DisplayName("Flickering clears counters, damage, and tapped state")
    void returnsAsANewUntappedPermanent() {
        harness.addToBattlefield(player1, new ConjurersCloset());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        creature.tap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setMarkedDamage(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Vorstclaw");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature owned by an opponent returns under the ability controller's control")
    void returnsOpponentOwnedCreatureUnderYourControl() {
        harness.addToBattlefield(player1, new ConjurersCloset());
        Vorstclaw card = new Vorstclaw();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        harness.addToBattlefieldAndReturn(player1, new ControlMagic()).setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Vorstclaw");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        harness.assertNotOnBattlefield(player2, "Vorstclaw");
        assertThat(gd.stolenCreatures).containsEntry(returned.getId(), player2.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A target that changes controllers before resolution is not flickered")
    void doesNotFlickerCreatureNoLongerControlledByYou() {
        harness.addToBattlefield(player1, new ConjurersCloset());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        harness.addToBattlefieldAndReturn(player2, new ControlMagic()).setAttachedTo(creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player2, "Vorstclaw").getId()).isEqualTo(creature.getId());
        harness.assertNotOnBattlefield(player1, "Vorstclaw");
    }
}
