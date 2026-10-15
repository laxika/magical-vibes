package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GolgariLocket;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PauseForReflection;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.cards.v.VraskaGolgariQueen;
import com.github.laxika.magicalvibes.cards.w.WaryOkapi;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HatcherySpider.class, Blaze.class, ColossalDreadmaw.class, GrizzlyBears.class,
        LlanowarElves.class, GolgariLocket.class, PauseForReflection.class,
        VernadiShieldmate.class, VraskaGolgariQueen.class, WaryOkapi.class})
class HatcherySpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger reveals as many cards as there are creature cards in the graveyard")
    void revealsCreatureCardsInGraveyardCount() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new ColossalDreadmaw();
        Card belowReveal = new LlanowarElves();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setLibrary(player1, List.of(eligible, tooExpensive, belowReveal));

        castHatcherySpider();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowReveal, tooExpensive);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hatchery Spider");
    }

    @Test
    @DisplayName("The controller may decline the green permanent and the revealed cards go to the bottom")
    void mayDeclinePuttingPermanentOntoBattlefield() {
        Card eligible = new LlanowarElves();
        Card belowReveal = new GrizzlyBears();

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(eligible, belowReveal));

        castHatcherySpider();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowReveal, eligible);
    }

    @Test
    @DisplayName("With no creature cards in the graveyard, the cast trigger reveals nothing")
    void noCreatureCardsRevealNothing() {
        Card topCard = new GrizzlyBears();

        harness.setGraveyard(player1, List.of(new Blaze()));
        harness.setLibrary(player1, List.of(topCard));

        castHatcherySpider();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hatchery Spider");
    }

    @Test
    @DisplayName("Only green permanents qualify, even when a colorless artifact can produce green mana")
    void excludesNonpermanentsAndColorlessPermanents() {
        Card eligible = new WaryOkapi();
        Card instant = new PauseForReflection();
        Card artifact = new GolgariLocket();
        Card belowReveal = new VernadiShieldmate();
        harness.setGraveyard(player1, List.of(new WaryOkapi(), new WaryOkapi(), new WaryOkapi()));
        harness.setLibrary(player1, List.of(eligible, instant, artifact, belowReveal));

        castHatcherySpider();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.assertOnBattlefield(player1, "Wary Okapi");
        harness.assertNotOnBattlefield(player1, "Golgari Locket");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(belowReveal);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(instant, artifact);
    }

    @Test
    @DisplayName("The graveyard count and mana value limit are determined when the trigger resolves")
    void countsOnlyControllersCreaturesAtResolution() {
        Card first = new VernadiShieldmate();
        Card second = new VernadiShieldmate();
        Card belowReveal = new WaryOkapi();
        harness.setGraveyard(player1, List.of(new WaryOkapi()));
        harness.setGraveyard(player2, List.of(new WaryOkapi(), new WaryOkapi(), new WaryOkapi()));
        harness.setLibrary(player1, List.of(first, second, belowReveal));

        castHatcherySpider();
        harness.setGraveyard(player1, List.of(new WaryOkapi(), new WaryOkapi(), new PauseForReflection()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        harness.assertOnBattlefield(player1, "Vernadi Shieldmate");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowReveal, first);
        harness.assertNotOnBattlefield(player1, "Hatchery Spider");
    }

    @Test
    @DisplayName("A green noncreature permanent qualifies and a short library does not reduce the mana value limit")
    void putsMulticoloredPlaneswalkerOntoBattlefieldFromShortLibrary() {
        Card planeswalker = new VraskaGolgariQueen();
        harness.setGraveyard(player1, List.of(new WaryOkapi(), new WaryOkapi(), new WaryOkapi(), new WaryOkapi()));
        harness.setLibrary(player1, List.of(planeswalker));

        castHatcherySpider();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(planeswalker.getId()));

        harness.assertOnBattlefield(player1, "Vraska, Golgari Queen");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Hatchery Spider");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hatchery Spider");
    }

    @Test
    @DisplayName("Revealed cards go to the bottom without a choice when no card meets the mana value limit")
    void noEligibleCardsGoToBottomAutomatically() {
        Card tooExpensive = new WaryOkapi();
        Card belowReveal = new VernadiShieldmate();
        harness.setGraveyard(player1, List.of(new WaryOkapi()));
        harness.setLibrary(player1, List.of(tooExpensive, belowReveal));

        castHatcherySpider();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowReveal, tooExpensive);
        harness.assertNotOnBattlefield(player1, "Wary Okapi");
    }

    private void castHatcherySpider() {
        harness.setHand(player1, List.of(new HatcherySpider()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
