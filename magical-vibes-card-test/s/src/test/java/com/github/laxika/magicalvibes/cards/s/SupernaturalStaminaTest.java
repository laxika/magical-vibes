package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SupernaturalStamina.class, GrizzlyBears.class, GoForTheThroat.class,
        FountainOfYouth.class, Humble.class})
class SupernaturalStaminaTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Grants the targeted creature +2/+0 until end of turn")
    void grantsBoost() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new SupernaturalStamina()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new SupernaturalStamina()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Returns the creature to the battlefield tapped under its owner's control when it dies this turn")
    void returnsCreatureTappedWhenItDiesThisTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SupernaturalStamina(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Kill the targeted creature later the same turn
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        resolveStack();

        // The card returns to the battlefield under player2's (owner's) control, tapped
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .singleElement()
                .matches(Permanent::isTapped);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return the creature if it survives the turn")
    void doesNotReturnWhenCreatureSurvives() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new SupernaturalStamina()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreature(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SupernaturalStamina()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void abilityRemovalPreventsReturn() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new SupernaturalStamina(), new Humble(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void stolenCreatureControllerControlsDeathTriggerButOwnerGetsCreature() {
        Permanent target = addCreature(player1);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new SupernaturalStamina(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getControllerId()).isEqualTo(player1.getId()));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.findPermanentById(gd, harness.getPermanentId(player2, "Grizzly Bears")).isTapped()).isTrue();
    }

    @Test
    void returnedCreatureDoesNotRetainBoostOrDeathAbility() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new SupernaturalStamina(), new GoForTheThroat(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player2, "Grizzly Bears"));
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        harness.castInstant(player1, 0, returned.getId());
        resolveStack();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void deathAbilityExpiresAtCleanup() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new SupernaturalStamina(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }
}
