package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BorderlandBehemoth;
import com.github.laxika.magicalvibes.cards.c.ChangelingSentinel;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.s.SageOfFables;
import com.github.laxika.magicalvibes.cards.v.VioletPall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingGraybeard.class, SageOfFables.class, ElvishWarrior.class,
        BorderlandBehemoth.class, ChangelingSentinel.class, VioletPall.class})
class WanderingGraybeardTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new SageOfFables()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing the shared-type card gains 4 life")
    void revealGainsLife() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new SageOfFables()));

        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Declining to reveal gains no life")
    void decliningDoesNothing() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new SageOfFables()));

        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Revealing leaves the card on top of the library")
    void revealingLeavesCardOnTop() {
        addCreatureReady(player1, new WanderingGraybeard());
        SageOfFables topCard = new SageOfFables();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Sharing Giant alone is enough to gain life")
    void giantOnTopGainsLife() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new BorderlandBehemoth()));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("A changeling on top shares a creature type")
    void changelingOnTopGainsLife() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new ChangelingSentinel()));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Kinship does not trigger on an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new SageOfFables()));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Kinship uses last known creature types after its source is destroyed")
    void destroyedSourceStillGainsLife() {
        var graybeard = addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new SageOfFables()));
        harness.setHand(player2, List.of(new VioletPall()));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, graybeard.getId());
        harness.assertInGraveyard(player1, "Wandering Graybeard");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Kinship lets its controller see even a nonmatching top card privately")
    void nonmatchingTopCardIsShownOnlyToController() {
        addCreatureReady(player1, new WanderingGraybeard());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.clearMessages();
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("Elvish Warrior")).isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("Elvish Warrior")).isEmpty();
    }

}
