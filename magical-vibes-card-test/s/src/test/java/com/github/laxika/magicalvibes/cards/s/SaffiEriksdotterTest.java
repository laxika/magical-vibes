package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaffiEriksdotter.class, SuddenDeath.class, AshcoatBear.class, ChromaticStar.class})
class SaffiEriksdotterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the targeted creature when it is put into the graveyard this turn")
    void returnsTargetedCreatureWhenItDiesThisTurn() {
        addPermanentReady(player1, new SaffiEriksdotter());
        Permanent bears = addPermanentReady(player1, new AshcoatBear());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ashcoat Bear");
        harness.assertNotInGraveyard(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Saffi Eriksdotter");
    }

    @Test
    @DisplayName("Does not return the targeted creature after the turn ends")
    void doesNotReturnAfterTurnEnds() {
        addPermanentReady(player1, new SaffiEriksdotter());
        Permanent bears = addPermanentReady(player1, new AshcoatBear());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreaturePermanent() {
        addPermanentReady(player1, new SaffiEriksdotter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticStar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Does not return a creature that goes to another player's graveyard")
    void doesNotReturnCreatureFromAnotherPlayersGraveyard() {
        addPermanentReady(player1, new SaffiEriksdotter());
        Permanent stolenBears = addPermanentReady(player1, new AshcoatBear());
        gd.stolenCreatures.put(stolenBears.getId(), player2.getId());

        harness.activateAbility(player1, 0, null, stolenBears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, stolenBears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Ashcoat Bear");
    }

    private Permanent addPermanentReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Can target itself, but the sacrificed Saffi does not return")
    void selfTargetDoesNotReturnSaffi() {
        Permanent saffi = harness.addToBattlefieldAndReturn(player1, new SaffiEriksdotter());

        harness.activateAbility(player1, 0, null, saffi.getId());
        harness.assertInGraveyard(player1, "Saffi Eriksdotter");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Saffi Eriksdotter");
        harness.assertInGraveyard(player1, "Saffi Eriksdotter");
    }

    @Test
    @DisplayName("Does not return a creature that dies before Saffi's ability resolves")
    void targetDiesBeforeAbilityResolves() {
        harness.addToBattlefield(player1, new SaffiEriksdotter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.activateAbility(player1, 0, null, bears.getId());

        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("The delayed ability returns the creature only once")
    void returnedCreatureIsNotProtectedFromSecondDeath() {
        harness.addToBattlefield(player1, new SaffiEriksdotter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SuddenDeath(), new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Ashcoat Bear");

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Ashcoat Bear"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Returns an owned creature controlled by the opponent under its owner's control")
    void returnsOwnedCreatureControlledByOpponent() {
        harness.addToBattlefield(player1, new SaffiEriksdotter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        gd.stolenCreatures.put(bears.getId(), player1.getId());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertNotInGraveyard(player1, "Ashcoat Bear");
    }
}
