package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ConcertedCare;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalinLoremaster.class, BofurReliableGuardian.class, ConcertedCare.class,
        FountainOfYouth.class, GrizzlyBears.class})
class BalinLoremasterTest extends BaseCardTest {

    @Test
    void mayDiscardHandAndDrawThatManyOnOwnEntryWithoutEnduringStoryDamage() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new BalinLoremaster());
        chooseBalinAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void decliningTheAbilityLeavesTheHandUntouched() {
        harness.addToBattlefield(player1, new BalinLoremaster());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new BofurReliableGuardian());
        chooseBalinAbility(false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void enduringStoryMakesTheAbilityDealDamageEqualToDiscardedCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new BalinLoremaster());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        chooseBalinAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private void chooseBalinAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
    }

    @Test
    void anotherDwarfTriggersDiscardAndDraw() {
        harness.addToBattlefield(player1, new BalinLoremaster());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));

        harness.enterBattlefieldAndReturn(player1, new BofurReliableGuardian());
        chooseBalinAbility(true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertLife(player2, 20);
    }

    @Test
    void nonDwarfAndOpponentsDwarfDoNotTrigger() {
        harness.addToBattlefield(player1, new BalinLoremaster());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.stack).isEmpty();
        harness.enterBattlefieldAndReturn(player2, new BofurReliableGuardian());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyHandDrawsNoCardsAndDealsNoDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new BalinLoremaster());

        chooseBalinAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageStillOccursAfterSourceAndQualifyingArtifactsLeave() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        var firstArtifact = harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        var secondArtifact = harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        var balin = harness.enterBattlefieldAndReturn(player1, new BalinLoremaster());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstArtifact);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondArtifact);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, balin);

        chooseBalinAbility(true);

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed(Conspiracy.class)
    void ownEntryTriggersEvenWhenBalinIsNoLongerADwarf() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));

        harness.enterBattlefieldAndReturn(player1, new BalinLoremaster());
        chooseBalinAbility(true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Fountain of Youth");
    }
}
