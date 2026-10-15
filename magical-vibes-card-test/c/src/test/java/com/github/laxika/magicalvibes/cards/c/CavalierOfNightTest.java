package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.StoneGolem;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavalierOfNight.class, GrizzlyBears.class, StoneGolem.class, WrathOfGod.class,
        GreenwoodSentinel.class, CentaurCourser.class, Murder.class})
class CavalierOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature destroys a target creature an opponent controls")
    void sacrificeAnotherCreatureDestroysTargetOpponentCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCavalier();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cavalier of Night");
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the battlefield unchanged")
    void decliningSacrificeDoesNothing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castCavalier();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cavalier of Night");
    }

    @Test
    @DisplayName("Death trigger targets only a creature card with mana value 3 or less from your graveyard")
    void deathReturnsCheapCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new CavalierOfNight());
        Card cheapCreature = new GrizzlyBears();
        Card expensiveCreature = new StoneGolem();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cheapCreature, expensiveCreature)));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).contains(cheapCreature.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(cheapCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Stone Golem");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Another creature can be sacrificed even with no opposing creature to target")
    void sacrificeWithoutLegalDestructionTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        castCavalier();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Cavalier of Night");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cavalier cannot sacrifice itself when it is the only creature its controller controls")
    void cannotSacrificeItself() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        castCavalier();
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertOnBattlefield(player1, "Cavalier of Night");
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destruction waits for the separate reflexive trigger to resolve")
    void destructionIsSeparateFromSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());
        castCavalier();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player2, "Centaur Courser");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Centaur Courser");
        harness.assertNotOnBattlefield(player2, "Centaur Courser");
    }

    @Test
    @DisplayName("Death returns a mana value three creature but cannot target noncreatures or opposing graveyards")
    void deathReturnsManaValueThreeCreatureOnlyFromOwnGraveyard() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfNight());
        Card eligible = new CentaurCourser();
        Card tooExpensive = new StoneGolem();
        Card noncreature = new Murder();
        Card opponentsCreature = new GreenwoodSentinel();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive, noncreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, cavalier.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertNotInGraveyard(player1, "Centaur Courser");
        harness.assertInGraveyard(player1, "Stone Golem");
        harness.assertInGraveyard(player1, "Murder");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Death with no eligible creature card produces no graveyard choice")
    void deathWithoutEligibleGraveyardCard() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfNight());
        harness.setGraveyard(player1, List.of(new StoneGolem()));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, cavalier.getId());

        harness.assertInGraveyard(player1, "Cavalier of Night");
        harness.assertInGraveyard(player1, "Stone Golem");
        harness.assertNotOnBattlefield(player1, "Stone Golem");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castCavalier() {
        harness.castFromHand(player1, new CavalierOfNight(), "{2}{B}{B}{B}");
    }
}
