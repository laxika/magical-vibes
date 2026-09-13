package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.cards.s.SquirrelWrangler;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Overburden.class, DivingGriffin.class, RhysticCave.class, SquirrelWrangler.class})
class OverburdenTest extends BaseCardTest {

    @Test
    @DisplayName("The creature's controller chooses a land to return")
    void creaturesControllerChoosesLand() {
        harness.addToBattlefield(player1, new Overburden());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        harness.setHand(player2, List.of(new DivingGriffin()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(player2Land.getId());
        assertThat(choice.context()).isInstanceOf(PermanentChoiceContext.BounceCreature.class);

        harness.handlePermanentChosen(player2, player2Land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Land);
        harness.assertInHand(player2, "Rhystic Cave");
        harness.assertOnBattlefield(player2, "Diving Griffin");
    }

    @Test
    @DisplayName("Token creatures do not trigger Overburden")
    void tokenCreaturesDoNotTrigger() {
        harness.addToBattlefield(player1, new Overburden());
        addCreatureReady(player2, new SquirrelWrangler());
        harness.addToBattlefield(player2, new RhysticCave());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Rhystic Cave");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getName().equals("Squirrel"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Noncreature permanents do not trigger Overburden")
    void noncreaturePermanentsDoNotTrigger() {
        harness.addToBattlefield(player1, new Overburden());
        harness.setHand(player2, List.of(new RhysticCave()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Rhystic Cave");
    }

    @Test
    @DisplayName("A nontoken creature with no land controller has nothing to return")
    void creatureControllerWithNoLandHasNothingToReturn() {
        harness.addToBattlefield(player1, new Overburden());
        harness.setHand(player2, List.of(new DivingGriffin()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Diving Griffin");
    }

    @Test
    @DisplayName("A controlled land returns to its owner's hand")
    void controlledLandReturnsToItsOwnerHand() {
        harness.addToBattlefield(player1, new Overburden());
        RhysticCave ownedLandCard = new RhysticCave();
        ownedLandCard.setOwnerId(player1.getId());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, ownedLandCard);
        harness.setHand(player2, List.of(new DivingGriffin()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(player2Land.getId());

        harness.handlePermanentChosen(player2, player2Land.getId());

        harness.assertNotOnBattlefield(player2, "Rhystic Cave");
        harness.assertInHand(player1, "Rhystic Cave");
    }
}
