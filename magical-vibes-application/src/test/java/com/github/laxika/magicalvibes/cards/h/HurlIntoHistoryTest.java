package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HurlIntoHistory.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, MightOfOaks.class, Millstone.class, Plains.class})
class HurlIntoHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact spell and discovers a card with equal mana value")
    void countersArtifactAndDiscoversEqualManaValueCard() {
        Millstone millstone = new Millstone();
        GrizzlyBears discovered = new GrizzlyBears();
        LlanowarElves belowDiscovered = new LlanowarElves();
        Plains land = new Plains();
        HillGiant tooExpensive = new HillGiant();
        harness.setLibrary(player2, List.of(land, tooExpensive, discovered, belowDiscovered));

        castHurlAtArtifact(millstone);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Millstone");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(land, tooExpensive, belowDiscovered);
    }

    @Test
    @DisplayName("Putting the discovered card into hand still counters the target")
    void putsDiscoveredCardIntoHandWhenDeclined() {
        GrizzlyBears target = new GrizzlyBears();
        Millstone discovered = new Millstone();
        Plains land = new Plains();
        HillGiant tooExpensive = new HillGiant();
        harness.setLibrary(player2, List.of(land, tooExpensive, discovered));

        castHurlAtCreature(target);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(land, tooExpensive);
    }

    @Test
    @DisplayName("Puts a discovered card into hand when it has no legal free-cast target")
    void putsUncastableDiscoveredCardIntoHand() {
        GrizzlyBears target = new GrizzlyBears();
        GiantGrowth discovered = new GiantGrowth();
        harness.setLibrary(player2, List.of(discovered));

        castHurlAtCreature(target);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(discovered);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == discovered);
    }

    @Test
    @DisplayName("Counters the target and bottoms all cards when discover finds no match")
    void bottomsAllCardsWhenDiscoverFindsNoMatch() {
        Millstone target = new Millstone();
        Plains land = new Plains();
        HillGiant tooExpensive = new HillGiant();
        harness.setLibrary(player2, List.of(land, tooExpensive));

        castHurlAtArtifact(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Millstone");
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(land, tooExpensive);
    }

    @Test
    @DisplayName("Cannot target a nonartifact, noncreature spell")
    void cannotTargetNonArtifactNoncreatureSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        HurlIntoHistory hurl = new HurlIntoHistory();
        harness.setHand(player2, List.of(hurl));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, might.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHurlAtArtifact(Millstone artifact) {
        harness.setHand(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new HurlIntoHistory()));
        addHurlMana();

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();
    }

    private void castHurlAtCreature(GrizzlyBears creature) {
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new HurlIntoHistory()));
        addHurlMana();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void addHurlMana() {
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
    }
}
