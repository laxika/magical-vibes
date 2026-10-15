package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldsHorn.class, GrizzlyBears.class, WalkingCorpse.class})
class HeraldsHornTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Herald's Horn prompts for a creature type")
    void choosesCreatureTypeOnEntry() {
        harness.setHand(player1, List.of(new HeraldsHorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Herald's Horn").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Creature spells of the chosen type cost {1} less")
    void reducesChosenTypeSpellCost() {
        addHorn(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature spells without the chosen type are not reduced")
    void doesNotReduceOtherTypeSpellCost() {
        addHorn(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The upkeep ability may reveal a matching top card to hand")
    void matchingTopCardGoesToHand() {
        addHorn(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("A nonmatching top card stays on top")
    void nonmatchingTopCardStaysOnTop() {
        addHorn(CardSubtype.BEAR);
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setLibrary(player1, List.of(corpse));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(corpse);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(corpse);
    }

    @Test
    @DisplayName("Declining a matching card leaves it on top of the library")
    void mayDeclineMatchingCard() {
        addHorn(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("An empty library does not offer a reveal choice")
    void emptyLibraryDoesNotOfferChoice() {
        addHorn(CardSubtype.BEAR);
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("A noncreature top card stays on top without a reveal choice")
    void noncreatureTopCardStaysOnTop() {
        addHorn(CardSubtype.BEAR);
        HeraldsHorn topCard = new HeraldsHorn();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("The cost reduction cannot pay the colored part of a spell's cost")
    void doesNotReduceColoredManaCost() {
        addHorn(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Horn does not reduce your creature spells")
    void opponentsHornDoesNotReduceCost() {
        Permanent horn = harness.addToBattlefieldAndReturn(player2, new HeraldsHorn());
        horn.setChosenSubtype(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An upkeep trigger retains the chosen type after its Horn leaves the battlefield")
    void upkeepTriggerUsesChosenTypeAfterSourceLeaves() {
        Permanent horn = addHorn(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, horn));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player1, "Herald's Horn");
    }

    @Test
    @DisplayName("Looking at a nonmatching card shows its identity only to the controller")
    void nonmatchingCardIsPrivatelyShownToController() {
        addHorn(CardSubtype.BEAR);
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setLibrary(player1, List.of(corpse));

        advanceToUpkeep(player1);
        harness.clearMessages();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(corpse);
        assertThat(harness.getConn1().getMessagesContaining("Walking Corpse")).isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("Walking Corpse")).isEmpty();
    }

    private Permanent addHorn(CardSubtype chosenSubtype) {
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new HeraldsHorn());
        horn.setChosenSubtype(chosenSubtype);
        return horn;
    }
}
