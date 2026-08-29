package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({AridArchway.class, GrizzlyBears.class, HostileDesert.class, Island.class})
class AridArchwayTest extends BaseCardTest {

    @Test
    @DisplayName("Returning another Desert surveils 1")
    void returningAnotherDesertSurveils() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new HostileDesert());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new AridArchway()));

        harness.playLand(player1, 0);
        Permanent archway = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Arid Archway"))
                .findFirst()
                .orElseThrow();
        assertThat(archway.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(desert.getId(), archway.getId());

        harness.handlePermanentChosen(player1, desert.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        harness.assertInHand(player1, "Hostile Desert");
    }

    @Test
    @DisplayName("Returning a non-Desert does not surveil")
    void returningNonDesertDoesNotSurveil() {
        Card topCard = new GrizzlyBears();
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
}
