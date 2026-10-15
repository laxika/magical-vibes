package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.c.CompassGnome;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TheCore;
import com.github.laxika.magicalvibes.cards.z.ZoeticGlyph;
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

@CardUsed({MatzalantliTheGreatDoor.class, TheCore.class, DarksteelRelic.class, DelugeOfTheDead.class,
        Forest.class, GloriousAnthem.class, GrizzlyBears.class, InvasionOfInnistrad.class,
        JaceBeleren.class, Shock.class, Abrade.class, CompassGnome.class, ZoeticGlyph.class})
class MatzalantliTheGreatDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and then prompts for a discard")
    void drawsThenDiscards() {
        Permanent door = addReadyDoor();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(door.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Transforms when the graveyard has four distinct permanent types")
    void transformsAtFourPermanentTypes() {
        Permanent door = addReadyDoor();
        harness.setGraveyard(player1, List.of(
                new DarksteelRelic(), new GrizzlyBears(), new GloriousAnthem(),
                new Forest(), new InvasionOfInnistrad(), new JaceBeleren()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(door.isTransformed()).isTrue();
        assertThat(door.isTapped()).isTrue();
        assertThat(door.getCard()).isInstanceOf(TheCore.class);
    }

    @Test
    @DisplayName("Does not activate with only three permanent types plus instants")
    void doesNotActivateBelowFourPermanentTypes() {
        Permanent door = addReadyDoor();
        harness.setGraveyard(player1, List.of(
                new DarksteelRelic(), new GrizzlyBears(), new GloriousAnthem(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more permanent types");

        assertThat(door.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("The Core adds mana equal to the number of permanent cards in the graveyard")
    void coreAddsManaForPermanentCards() {
        Permanent core = addTransformedDoor();
        harness.setGraveyard(player1, List.of(
                new DarksteelRelic(), new GrizzlyBears(), new GloriousAnthem(), new Forest(), new Shock()));

        harness.activateAbility(player1, battlefieldIndex(core), 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    @DisplayName("One artifact creature supplies two of the four permanent types")
    void transformsWithFourTypesOnThreeCards() {
        Permanent door = addReadyDoor();
        harness.setGraveyard(player1, List.of(new CompassGnome(), new ZoeticGlyph(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(door.isTransformed()).isTrue();
        assertThat(door.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Transform restriction is checked at activation, not resolution")
    void transformsAfterGraveyardIsEmptiedInResponse() {
        Permanent door = addReadyDoor();
        harness.setGraveyard(player1, List.of(new CompassGnome(), new ZoeticGlyph(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(door.isTransformed()).isTrue();
        assertThat(door.getCard()).isInstanceOf(TheCore.class);
    }

    @Test
    @DisplayName("Opponent's permanent types cannot satisfy the transform restriction")
    void ignoresOpponentsGraveyardForActivation() {
        Permanent door = addReadyDoor();
        harness.setGraveyard(player1, List.of(new CompassGnome(), new Forest()));
        harness.setGraveyard(player2, List.of(new ZoeticGlyph()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more permanent types");

        assertThat(door.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("The Core counts cards once, excludes instants, and ignores opposing graveyards")
    void coreCountsCardsRatherThanTypes() {
        Permanent core = addTransformedDoor();
        harness.setGraveyard(player1, List.of(new CompassGnome(), new CompassGnome(), new Abrade()));
        harness.setGraveyard(player2, List.of(new Forest(), new ZoeticGlyph()));

        harness.activateAbility(player1, battlefieldIndex(core), 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(core.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("The Core can activate with an empty graveyard and produces no mana")
    void coreProducesZeroWithEmptyGraveyard() {
        Permanent core = addTransformedDoor();
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, battlefieldIndex(core), 0, null, null);

        assertThat(core.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An empty hand still discards the card just drawn")
    void discardsDrawnCardFromInitiallyEmptyHand() {
        addReadyDoor();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyDoor() {
        Permanent door = harness.addToBattlefieldAndReturn(player1, new MatzalantliTheGreatDoor());
        door.setSummoningSick(false);
        return door;
    }

    private Permanent addTransformedDoor() {
        MatzalantliTheGreatDoor card = new MatzalantliTheGreatDoor();
        Permanent door = harness.addToBattlefieldAndReturn(player1, card);
        door.setSummoningSick(false);
        door.setCard(card.getBackFaceCard());
        door.setTransformed(true);
        return door;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
