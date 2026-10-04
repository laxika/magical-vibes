package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WarBehemoth;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForceAway.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        Forest.class, AlpineGrizzly.class, WetlandSambar.class, WarBehemoth.class})
class ForceAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature and offers ferocious draw and discard")
    void returnsCreatureAndOffersFerociousDrawAndDiscard() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForceAway(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not offer ferocious draw without a creature with power 4 or greater")
    void doesNotOfferFerociousDrawWithoutLargeCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Declining ferocious draw does not discard")
    void decliningFerociousDrawDoesNotDiscard() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForceAway(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returningOnlyFerociousCreatureDoesNotOfferDraw() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.setLibrary(player1, List.of(new WetlandSambar()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Alpine Grizzly"));

        harness.assertInHand(player1, "Alpine Grizzly");
        harness.assertNotInHand(player1, "Wetland Sambar");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsLargeCreatureDoesNotEnableFerocious() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Wetland Sambar"));

        harness.assertInHand(player2, "Wetland Sambar");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canDiscardPreviouslyHeldCardAndKeepDrawnCard() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new ForceAway(), new Forest()));
        harness.setLibrary(player1, List.of(new WetlandSambar()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Wetland Sambar"));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Wetland Sambar");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void illegalTargetPreventsFerociousDraw() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.setHand(player2, List.of(new ForceAway()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        var targetId = harness.getPermanentId(player2, "Wetland Sambar");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Wetland Sambar");
        harness.assertInGraveyard(player1, "Force Away");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void losingFerociousCreatureBeforeResolutionPreventsDraw() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.setHand(player2, List.of(new ForceAway()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Wetland Sambar"));
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Alpine Grizzly"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Alpine Grizzly");
        harness.assertInHand(player2, "Wetland Sambar");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void creatureWithPowerThreeDoesNotEnableFerocious() {
        harness.addToBattlefield(player1, new WarBehemoth());
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Wetland Sambar"));

        harness.assertInHand(player2, "Wetland Sambar");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
