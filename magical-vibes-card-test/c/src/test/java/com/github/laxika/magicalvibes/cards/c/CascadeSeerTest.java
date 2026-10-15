package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.StoneworkPackbeast;
import com.github.laxika.magicalvibes.cards.k.KitesailCleric;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CascadeSeer.class, BoggartBrute.class, FaerieMiscreant.class, Forest.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class,
        StoneworkPackbeast.class, KitesailCleric.class})
class CascadeSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Scry amount equals the size of its controller's party")
    void scriesForPartySize() {
        addFullParty();
        List<Card> library = List.of(new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        castCascadeSeer();

        harness.passBothPriorities();
        resolveAllTriggers();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactlyElementsOf(library);

        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.ScryOrder(List.of(3, 2, 1, 0), List.of()));

        assertThat(gameData.playerDecks.get(player1.getId()))
                .containsSubsequence(library.get(3), library.get(2), library.get(1), library.get(0));
    }

    @Test
    @DisplayName("A party creature can fill only one role")
    void oneCreatureFillsOnlyOneRole() {
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        List<Card> library = List.of(new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        castCascadeSeer();

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactlyElementsOf(library.subList(0, 3));
    }

    @Test
    @DisplayName("Cascade Seer counts itself but ignores opposing party members")
    void countsItselfAndIgnoresOpponents() {
        harness.addToBattlefield(player2, new KitesailCleric());
        harness.addToBattlefield(player2, new StoneworkPackbeast());
        List<Card> library = List.of(new CascadeSeer(), new KitesailCleric(), new StoneworkPackbeast());
        harness.setLibrary(player1, library);
        castCascadeSeer();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.getFirst());
    }

    @Test
    @DisplayName("Party size is evaluated when the enter trigger resolves")
    void countsPartyAtResolution() {
        List<Card> library = List.of(new CascadeSeer(), new KitesailCleric(), new StoneworkPackbeast());
        harness.setLibrary(player1, library);
        castCascadeSeer();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new KitesailCleric());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactlyElementsOf(library.subList(0, 2));
    }

    @Test
    @DisplayName("Removing Cascade Seer before its trigger resolves can reduce the party to zero")
    void sourceLeavingCanMakeScryZero() {
        List<Card> library = List.of(new CascadeSeer(), new KitesailCleric());
        harness.setLibrary(player1, library);
        castCascadeSeer();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Multiple Packbeasts fill distinct roles for a full party")
    void maximizesRolesAndOrdersTopAndBottom() {
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addToBattlefield(player1, new KitesailCleric());
        List<Card> library = List.of(new CascadeSeer(), new KitesailCleric(),
                new StoneworkPackbeast(), new CascadeSeer(), new KitesailCleric());
        harness.setLibrary(player1, library);
        castCascadeSeer();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactlyElementsOf(library.subList(0, 4));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0), List.of(3, 1)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), library.get(0), library.get(4), library.get(3), library.get(1));
    }

    @Test
    @DisplayName("Scry examines only available cards when the library is smaller than the party")
    void handlesShortLibrary() {
        harness.addToBattlefield(player1, new KitesailCleric());
        List<Card> library = List.of(new StoneworkPackbeast());
        harness.setLibrary(player1, library);
        castCascadeSeer();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactlyElementsOf(library);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Scry with an empty library does not request an ordering choice")
    void handlesEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castCascadeSeer();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castCascadeSeer() {
        harness.castFromHand(player1, new CascadeSeer(), "{3}{U}");
    }

}
