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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbandonedSarcophagusTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a cycling instant from graveyard")
    void canCastCyclingInstantFromGraveyard() {
        harness.addToBattlefield(player1, new AbandonedSarcophagus());
        InfernoJet jet = new InfernoJet();
        harness.setGraveyard(player1, List.of(jet));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Inferno Jet"));
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Striped Riverwinder"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Striped Riverwinder"));
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
        gd.playerDecks.put(player1.getId(), new java.util.ArrayList<>(List.of(new Shock())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Countervailing Winds"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Countervailing Winds"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Lurching Rotbeast"));
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
