package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlamekinBladewhirl;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupremeExemplar.class, FlamekinBladewhirl.class, GrizzlyBears.class, Unsummon.class, RayOfCommand.class})
class SupremeExemplarTest extends BaseCardTest {

    private void castSupremeExemplar() {
        harness.castFromHand(player1, new SupremeExemplar(), "{6}{U}");
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no other Elemental")
    void autoSacrificesWithNoOtherElemental() {
        castSupremeExemplar();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Supreme Exemplar");
        harness.assertInGraveyard(player1, "Supreme Exemplar");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An Elemental controlled by an opponent does not satisfy the champion cost")
    void opponentElementalDoesNotSatisfyChampion() {
        harness.addToBattlefield(player2, new FlamekinBladewhirl());
        castSupremeExemplar();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Supreme Exemplar");
        harness.assertOnBattlefield(player2, "Flamekin Bladewhirl");
        harness.assertInGraveyard(player1, "Supreme Exemplar");
    }

    @Test
    @DisplayName("A non-Elemental creature does not satisfy the champion cost")
    void nonElementalDoesNotSatisfyChampion() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castSupremeExemplar();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice (no valid Elemental)

        harness.assertNotOnBattlefield(player1, "Supreme Exemplar");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Supreme Exemplar");
    }

    @Test
    @DisplayName("ETB with another Elemental prompts champion choice")
    void etbWithElementalPromptsChoice() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castSupremeExemplar();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertOnBattlefield(player1, "Supreme Exemplar");
    }

    @Test
    @DisplayName("Championing an Elemental exiles it and keeps Supreme Exemplar")
    void championingExilesElemental() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castSupremeExemplar();
        harness.passBothPriorities();

        UUID elementalId = harness.getPermanentId(player1, "Flamekin Bladewhirl");
        harness.handlePermanentChosen(player1, elementalId);

        harness.assertOnBattlefield(player1, "Supreme Exemplar");
        harness.assertNotOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flamekin Bladewhirl"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Championed Elemental returns when Supreme Exemplar leaves the battlefield")
    void championedElementalReturnsWhenExemplarLeaves() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castSupremeExemplar();
        harness.passBothPriorities();

        UUID elementalId = harness.getPermanentId(player1, "Flamekin Bladewhirl");
        harness.handlePermanentChosen(player1, elementalId);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID exemplarId = harness.getPermanentId(player1, "Supreme Exemplar");
        harness.castAndResolveInstant(player1, 0, exemplarId);

        harness.assertNotOnBattlefield(player1, "Supreme Exemplar");
        harness.assertNotOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve the champion return trigger

        harness.assertOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Flamekin Bladewhirl"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Controller may decline champion even with an Elemental available")
    void mayDeclineChampionWithElementalAvailable() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castSupremeExemplar();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertInGraveyard(player1, "Supreme Exemplar");
        harness.assertNotOnBattlefield(player1, "Supreme Exemplar");
        harness.assertOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Elemental exiled after Supreme Exemplar leaves remains exiled")
    void championAfterSourceLeavesDoesNotReturnElemental() {
        harness.addToBattlefield(player1, new FlamekinBladewhirl());
        castSupremeExemplar();

        UUID exemplarId = harness.getPermanentId(player1, "Supreme Exemplar");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, exemplarId);
        harness.assertNotOnBattlefield(player1, "Supreme Exemplar");

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Flamekin Bladewhirl"));

        harness.assertNotOnBattlefield(player1, "Flamekin Bladewhirl");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flamekin Bladewhirl"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Champion cannot sacrifice Supreme Exemplar after an opponent gains control")
    void cannotSacrificeSourceControlledByOpponent() {
        castSupremeExemplar();

        UUID exemplarId = harness.getPermanentId(player1, "Supreme Exemplar");
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, exemplarId);
        harness.assertOnBattlefield(player2, "Supreme Exemplar");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Supreme Exemplar");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Supreme Exemplar"));
    }
}
