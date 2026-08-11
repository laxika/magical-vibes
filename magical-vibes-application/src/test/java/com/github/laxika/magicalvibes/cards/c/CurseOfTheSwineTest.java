package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurseOfTheSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles X creatures and gives each exiled creature's controller a Boar")
    void exilesTargetsAndCreatesBoarsForTheirControllers() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 2, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Hill Giant");
        assertThat(boarsControlledBy(player1.getId())).hasSize(1);
        assertThat(boarsControlledBy(player2.getId())).hasSize(1);
        assertThat(boarsControlledBy(player1.getId()).getFirst().getCard().getSubtypes())
                .containsExactly(CardSubtype.BOAR);
    }

    @Test
    @DisplayName("X=0 exiles no creatures and creates no Boars")
    void xZeroDoesNothing() {
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(boarsControlledBy(player1.getId())).isEmpty();
        assertThat(boarsControlledBy(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target more creatures than X")
    void cannotTargetMoreThanX() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private List<Permanent> boarsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(permanent -> "Boar".equals(permanent.getCard().getName()))
                .toList();
    }
}
