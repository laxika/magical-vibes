package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.p.PressurePoint;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduWoeReaper.class, ArashinCleric.class, PressurePoint.class})
class MarduWoeReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Warrior ETB trigger can exile a creature card and gain 1 life")
    void ownEntryExilesCreatureAndGainsLife() {
        ArashinCleric creature = new ArashinCleric();
        harness.setGraveyard(player2, List.of(creature));
        int lifeBefore = gd.getLife(player1.getId());

        castWoeReaper();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Arashin Cleric");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("A noncreature card is not a legal target")
    void noncreatureCardIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new PressurePoint()));
        int lifeBefore = gd.getLife(player1.getId());

        castWoeReaper();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Pressure Point");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Another Warrior entering under its controller's control triggers the ability")
    void anotherWarriorEntryTriggersAbility() {
        ArashinCleric creature = new ArashinCleric();
        harness.setGraveyard(player2, List.of(creature));
        int lifeBefore = gd.getLife(player1.getId());

        harness.addToBattlefield(player1, new MarduWoeReaper());
        castWoeReaper();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player2, "Arashin Cleric");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void decliningExileDoesNotGainLife() {
        harness.setGraveyard(player2, List.of(new ArashinCleric()));
        int lifeBefore = gd.getLife(player1.getId());

        castWoeReaper();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Arashin Cleric");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void canExileCreatureFromOwnGraveyard() {
        ArashinCleric creature = new ArashinCleric();
        harness.setGraveyard(player1, List.of(creature));
        int lifeBefore = gd.getLife(player1.getId());

        castWoeReaper();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Arashin Cleric");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    void nonWarriorEntryDoesNotTriggerExile() {
        harness.addToBattlefield(player1, new MarduWoeReaper());
        harness.setGraveyard(player2, List.of(new ArashinCleric()));
        int lifeBefore = gd.getLife(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ArashinCleric(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Arashin Cleric");
        harness.assertLife(player1, lifeBefore + 3);
    }

    @Test
    void opponentsWarriorDoesNotTriggerOurWoeReaper() {
        harness.addToBattlefield(player1, new MarduWoeReaper());
        harness.setGraveyard(player1, List.of(new ArashinCleric()));
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new MarduWoeReaper(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Arashin Cleric");
    }

    @Test
    void choosesGraveyardTargetBeforeAbilityResolves() {
        harness.setGraveyard(player2, List.of(new ArashinCleric(), new ArashinCleric()));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MarduWoeReaper(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThat(((PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
    }

    private void castWoeReaper() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MarduWoeReaper(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
