package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AuriokBladewarden;
import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyrIncubator.class, GoldMyr.class, IronMyr.class, AuriokBladewarden.class, PsychogenicProbe.class, AvenMindcensor.class})
class MyrIncubatorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any number of artifact cards and creates one Myr for each")
    void exilesArtifactsAndCreatesMatchingNumberOfMyrs() {
        resolveActivation(List.of(new IronMyr(), new GoldMyr(), new AuriokBladewarden()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.ARTIFACT));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactlyInAnyOrder("Iron Myr", "Gold Myr");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Auriok Bladewarden");
        assertThat(findPermanents(player1, "Myr")).hasSize(2)
                .allSatisfy(myr -> {
                    assertThat(myr.getCard().getPower()).isEqualTo(1);
                    assertThat(myr.getCard().getToughness()).isEqualTo(1);
                    assertThat(myr.getCard().getColor()).isNull();
                    assertThat(myr.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(myr.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
                    assertThat(myr.getCard().getSubtypes()).contains(CardSubtype.MYR);
                    assertThat(myr.getCard().isToken()).isTrue();
                });
        harness.assertInGraveyard(player1, "Myr Incubator");
    }

    @Test
    @DisplayName("May stop the artifact search without selecting any card")
    void maySelectZeroArtifactCards() {
        resolveActivation(List.of(new IronMyr(), new GoldMyr()));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(findPermanents(player1, "Myr")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Myr Incubator");
    }

    @Test
    @DisplayName("May stop after selecting only some of the available artifacts")
    void maySelectOnlySomeArtifacts() {
        resolveActivation(List.of(new IronMyr(), new GoldMyr(), new AuriokBladewarden()));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(findPermanents(player1, "Myr")).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Iron Myr");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Gold Myr", "Auriok Bladewarden");
        assertThat(findPermanents(player1, "Myr")).hasSize(1);
        assertThat(findPermanents(player2, "Myr")).isEmpty();
    }

    @Test
    @DisplayName("Shuffles even when the library has no artifact cards")
    void shufflesWhenNoArtifactsAreFound() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        resolveActivation(List.of(new AuriokBladewarden()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(findPermanents(player1, "Myr")).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Shuffles an initially empty library and triggers Psychogenic Probe")
    void shufflesInitiallyEmptyLibrary() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        resolveActivation(List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(findPermanents(player1, "Myr")).isEmpty();
        harness.assertInGraveyard(player1, "Myr Incubator");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Shuffles after exiling every card in the library")
    void shufflesAfterExilingEntireLibrary() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        resolveActivation(List.of(new IronMyr()));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Myr")).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Pays the activation costs before resolution even when newly entered")
    void paysCostsBeforeResolution() {
        harness.setLibrary(player1, List.of(new IronMyr()));
        harness.addToBattlefield(player1, new MyrIncubator());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Myr Incubator");
        harness.assertInGraveyard(player1, "Myr Incubator");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(findPermanents(player1, "Myr")).hasSize(1);
    }

    @Test
    @DisplayName("Aven Mindcensor limits the entire search to the original top four cards")
    void mindcensorRestrictionPersistsAfterFirstSelection() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        resolveActivation(List.of(new IronMyr(), new AuriokBladewarden(),
                new AuriokBladewarden(), new AuriokBladewarden(), new GoldMyr()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Iron Myr");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Iron Myr");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .contains("Gold Myr");
        assertThat(findPermanents(player1, "Myr")).hasSize(1);
    }

    private void resolveActivation(List<com.github.laxika.magicalvibes.model.Card> library) {
        harness.setLibrary(player1, library);
        Permanent incubator = harness.addToBattlefieldAndReturn(player1, new MyrIncubator());
        incubator.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
