package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BristlingBackwoods;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PatientNaturalist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AridArchway.class, BristlingBackwoods.class, Island.class, PatientNaturalist.class})
class AridArchwayTest extends BaseCardTest {

    @Test
    @DisplayName("Returning another Desert surveils 1")
    void returningAnotherDesertSurveils() {
        Card topCard = new PatientNaturalist();
        harness.setLibrary(player1, List.of(topCard));
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new BristlingBackwoods());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new AridArchway()));

        harness.playLand(player1, 0);
        Permanent archway = findPermanent(player1, "Arid Archway");

        assertThat(archway.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(desert.getId(), archway.getId());

        harness.handlePermanentChosen(player1, desert.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        harness.assertInHand(player1, "Bristling Backwoods");
    }

    @Test
    @DisplayName("Returning a non-Desert does not surveil")
    void returningNonDesertDoesNotSurveil() {
        Card topCard = new PatientNaturalist();
        harness.setLibrary(player1, List.of(topCard));
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new AridArchway()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(island.getId(), harness.getPermanentId(player1, "Arid Archway"))
                .doesNotContain(opponentIsland.getId());
        harness.handlePermanentChosen(player1, island.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Choosing Arid Archway itself is legal when it is the only land")
    void canReturnItselfWhenItIsTheOnlyLand() {
        harness.setHand(player1, List.of(new AridArchway()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        UUID archwayId = harness.getPermanentId(player1, "Arid Archway");
        harness.handlePermanentChosen(player1, archwayId);

        harness.assertInHand(player1, "Arid Archway");
        harness.assertNotOnBattlefield(player1, "Arid Archway");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tapping Arid Archway adds two colorless mana")
    void tappingAddsTwoColorlessMana() {
        Permanent archway = harness.addToBattlefieldAndReturn(player1, new AridArchway());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(archway.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returning another copy of Arid Archway surveils and may keep the card on top")
    void returningAnotherArchwayCanKeepTopCard() {
        Card topCard = new PatientNaturalist();
        harness.setLibrary(player1, List.of(topCard));
        Permanent otherArchway = harness.addToBattlefieldAndReturn(player1, new AridArchway());
        harness.setHand(player1, List.of(new AridArchway()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherArchway.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        harness.assertInHand(player1, "Arid Archway");
        assertThat(findPermanents(player1, "Arid Archway")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Returning itself does not surveil even when another Desert is available")
    void returningItselfWithAnotherDesertDoesNotSurveil() {
        Card topCard = new PatientNaturalist();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new BristlingBackwoods());
        harness.setHand(player1, List.of(new AridArchway()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Arid Archway"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInHand(player1, "Arid Archway");
        harness.assertOnBattlefield(player1, "Bristling Backwoods");
    }

    @Test
    @DisplayName("Returning another Desert with an empty library completes without a choice")
    void returningDesertWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new BristlingBackwoods());
        harness.setHand(player1, List.of(new AridArchway()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, desert.getId());

        harness.assertInHand(player1, "Bristling Backwoods");
        harness.assertOnBattlefield(player1, "Arid Archway");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Desert exiled instead of returning to hand does not cause surveil")
    void exileReplacementDoesNotSurveil() {
        Card topCard = new PatientNaturalist();
        harness.setLibrary(player1, List.of(topCard));
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new BristlingBackwoods());
        desert.setExileIfLeavesBattlefield(true);
        harness.setHand(player1, List.of(new AridArchway()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, desert.getId());

        assertThat(gd.findExiledCard(desert.getCard().getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(desert.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
