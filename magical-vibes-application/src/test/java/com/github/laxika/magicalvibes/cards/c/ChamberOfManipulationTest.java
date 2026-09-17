package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChamberOfManipulation.class, AngelicWall.class, Forest.class})
class ChamberOfManipulationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land can tap and discard a card to gain control of a creature until end of turn")
    void enchantedLandGainsControlOfCreatureUntilEndOfTurn() {
        Permanent land = addChamberToLand();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(land.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Forest");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("The enchanted land's controller can activate the granted ability")
    void enchantedLandControllerCanActivateGrantedAbility() {
        Permanent land = addChamberToLand(player2, player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AngelicWall());
        harness.setHand(player2, List.of(new Forest()));

        harness.activateAbility(player2, 0, 0, null, creature.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("An activated ability resolves after Chamber of Manipulation leaves")
    void activatedAbilityResolvesAfterAuraLeaves() {
        Permanent land = addChamberToLand();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.handleCardChosen(player1, 0);
        Permanent aura = findPermanent(player1, "Chamber of Manipulation");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("The granted ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent land = addChamberToLand();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("card to activate ability");

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("The granted ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addChamberToLand();
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Removing Chamber of Manipulation removes the granted ability")
    void removingAuraRemovesGrantedAbility() {
        addChamberToLand();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        harness.setHand(player1, List.of(new Forest()));
        Permanent aura = findPermanent(player1, "Chamber of Manipulation");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addChamberToLand() {
        return addChamberToLand(player1, player1);
    }

    private Permanent addChamberToLand(Player landController, Player auraController) {
        Permanent land = harness.addToBattlefieldAndReturn(landController, new Forest());
        Permanent aura = new Permanent(new ChamberOfManipulation());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return land;
    }
}
