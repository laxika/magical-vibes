package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefenseOfTheHeart.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class DefenseOfTheHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger sacrifices the enchantment and puts up to two creatures onto the battlefield")
    void searchesForUpToTwoCreatures() {
        Permanent defense = castDefense();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(new Forest(), bears, elves));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == defense.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(defense.getCard());

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(bears, elves);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == bears)
                .anyMatch(p -> p.getCard() == elves);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger does not occur unless an opponent controls at least three creatures")
    void doesNotTriggerWithFewerThanThreeOpponentCreatures() {
        Permanent defense = castDefense();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(defense);
    }

    @Test
    @DisplayName("The trigger does not resolve when the condition is no longer met")
    void doesNotResolveWhenConditionIsNoLongerMet() {
        Permanent defense = castDefense();
        Permanent removableCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player2.getId()).remove(removableCreature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(defense);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(defense.getCard());
    }

    @Test
    @DisplayName("The trigger does not occur during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent defense = castDefense();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(defense);
    }

    @Test
    @DisplayName("Noncreature permanents do not count toward the trigger condition")
    void doesNotTriggerWithFewerThanThreeOpponentCreaturesAndALand() {
        Permanent defense = castDefense();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(defense);
    }

    @Test
    @DisplayName("With no creature cards in the library, the enchantment is still sacrificed")
    void sacrificesEvenWhenNoCreatureIsFound() {
        Permanent defense = castDefense();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == defense.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(defense.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may choose only one creature")
    void searchesForOnlyOneCreatureWhenSecondIsDeclined() {
        Permanent defense = castDefense();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(new Forest(), bears, elves));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int bearsIndex = search.params().cards().indexOf(bears);
        assertThat(bearsIndex).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(bearsIndex));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == bears)
                .noneMatch(p -> p.getCard() == elves)
                .noneMatch(p -> p.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).contains(elves);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(defense.getCard());
    }

    @Test
    @DisplayName("The controller may choose no creature even when matching cards are available")
    void mayChooseNoCreature() {
        Permanent defense = castDefense();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), bears));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() == bears);
        assertThat(gd.playerDecks.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(defense.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castDefense() {
        harness.castFromHand(player1, new DefenseOfTheHeart(), "{3}{G}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
