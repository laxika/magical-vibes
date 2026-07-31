package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GusthasScepterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability exiles a chosen card from hand face down, tracked with the Scepter")
    void exilesChosenCardFaceDown() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Ornithopter())));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        var exiled = gd.exiledCards.stream()
                .filter(e -> scepter.getId().equals(e.sourcePermanentId()))
                .toList();
        assertThat(exiled).singleElement().satisfies(e -> {
            assertThat(e.card().getName()).isEqualTo("Grizzly Bears");
            assertThat(e.faceDown()).isTrue();
            assertThat(e.ownerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("Second ability returns a card exiled with the Scepter to its owner's hand")
    void returnsExiledCardToHand() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        exileTopHandCard(scepter);
        scepter.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("Cards exiled with the Scepter go to their owner's graveyard when it leaves the battlefield")
    void exiledCardsGoToGraveyardWhenScepterLeaves() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        exileTopHandCard(scepter);

        harness.setHand(player2, new ArrayList<>(List.of(new Shatter())));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, scepter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("Cards exiled with the Scepter go to their owner's graveyard when another player gains control of it")
    void exiledCardsGoToGraveyardOnControlChange() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        exileTopHandCard(scepter);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, new ArrayList<>(List.of(new StealArtifact())));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, scepter.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(scepter.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("The first ability does nothing with an empty hand")
    void exileWithEmptyHandDoesNothing() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    private void exileTopHandCard(Permanent scepter) {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.exiledCards).anyMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }
}
