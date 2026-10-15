package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CountervailingWinds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InfernoJet;
import com.github.laxika.magicalvibes.cards.l.LurchingRotbeast;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StripedRiverwinder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbandonedSarcophagus.class, CountervailingWinds.class, GrizzlyBears.class,
        InfernoJet.class, LurchingRotbeast.class, Shock.class, StripedRiverwinder.class})
class AbandonedSarcophagusTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling spells cast from hand are also exiled on resolution")
    void cyclingSpellFromHandIsExiled() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.setHand(player1, List.of(new InfernoJet()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertNotInGraveyard(player1, "Inferno Jet");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Inferno Jet"));
    }

    @Test
    @DisplayName("Graveyard casting still requires the normal mana cost")
    void graveyardCastingRequiresMana() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.setGraveyard(player1, List.of(new StripedRiverwinder()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Striped Riverwinder");
        harness.assertNotOnBattlefield(player1, "Striped Riverwinder");
    }

    @Test
    @DisplayName("Graveyard permission does not grant sorceries instant timing")
    void graveyardSorceryCannotBeCastDuringCombat() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.setGraveyard(player1, List.of(new InfernoJet()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Inferno Jet");
    }

    @Test
    @DisplayName("Opponents cannot use your graveyard casting permission")
    void opponentCannotUsePermission() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.setGraveyard(player2, List.of(new StripedRiverwinder()));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
        harness.assertInGraveyard(player2, "Striped Riverwinder");
    }

    @Test
    @DisplayName("Opponent-owned cycling creatures go to their graveyard normally")
    void opponentCyclingCreatureIsNotExiled() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.addToBattlefield(player2, new LurchingRotbeast());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Lurching Rotbeast"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lurching Rotbeast");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Lurching Rotbeast"));
    }

    @Test
    @DisplayName("Can cast a cycling sorcery from graveyard")
    void canCastCyclingSorceryFromGraveyard() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        InfernoJet jet = new InfernoJet();
        harness.setGraveyard(player1, List.of(jet));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Inferno Jet");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Inferno Jet"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can cast a cycling creature from graveyard")
    void canCastCyclingCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.setGraveyard(player1, List.of(new StripedRiverwinder()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Striped Riverwinder");
        harness.assertNotInGraveyard(player1, "Striped Riverwinder");
    }

    @Test
    @DisplayName("Cannot cast a non-cycling card from graveyard")
    void cannotCastNonCyclingFromGraveyard() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Cycling a card puts it into the graveyard (not exile)")
    void cyclingPutsCardInGraveyard() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        CountervailingWinds winds = new CountervailingWinds();
        harness.setHand(player1, List.of(winds));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Shock()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Countervailing Winds");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Countervailing Winds"));
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("A cycling creature that dies is exiled instead of going to the graveyard")
    void dyingCyclingCreatureIsExiled() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.addToBattlefield(player1, new LurchingRotbeast());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        var targetId = harness.getPermanentId(player1, "Lurching Rotbeast");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Lurching Rotbeast");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lurching Rotbeast"));
    }

    @Test
    @DisplayName("Non-cycling cards still go to the graveyard normally")
    void nonCyclingCardsEnterGraveyard() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        var targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
