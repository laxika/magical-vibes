package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DenyTheWitch;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheirNumberIsLegion.class, MindStone.class, DenyTheWitch.class})
class TheirNumberIsLegionTest extends BaseCardTest {

    @Test
    @DisplayName("creates X tapped artifact Necron Warrior tokens and gains life for all artifacts")
    void createsTappedArtifactTokensAndGainsLife() {
        harness.addToBattlefield(player1, new MindStone());
        harness.setHand(player1, List.of(new TheirNumberIsLegion()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 2);

        List<Permanent> warriors = findPermanents(player1, "Necron Warrior");
        assertThat(warriors).hasSize(2);
        assertThat(warriors).allSatisfy(warrior -> {
            assertThat(warrior.isTapped()).isTrue();
            assertThat(warrior.getCard().hasType(CardType.ARTIFACT)).isTrue();
        });
        harness.assertLife(player1, 23);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Their Number Is Legion"));
    }

    @Test
    @DisplayName("can be cast from the graveyard and exiles after resolving")
    void castsFromGraveyard() {
        Card spell = new TheirNumberIsLegion();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(findPermanents(player1, "Necron Warrior")).hasSize(0);
        harness.assertNotInGraveyard(player1, "Their Number Is Legion");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Their Number Is Legion"));
    }

    @Test
    void zeroXStillCountsOnlyControllersArtifactsAtResolution() {
        harness.addToBattlefield(player2, new MindStone());
        harness.setHand(player1, List.of(new TheirNumberIsLegion()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new MindStone());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Necron Warrior")).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Their Number Is Legion");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Their Number Is Legion"));
    }

    @Test
    void castsFromGraveyardWithNonzeroX() {
        harness.setGraveyard(player1, List.of(new TheirNumberIsLegion()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.ensurePriority(player1);
        gs.playFlashbackSpell(gd, player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Necron Warrior"))
                .hasSize(3)
                .allSatisfy(warrior -> assertThat(warrior.isTapped()).isTrue());
        harness.assertLife(player1, 23);
        harness.assertNotInGraveyard(player1, "Their Number Is Legion");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Their Number Is Legion"));
    }

    @Test
    void counteredGraveyardCastReturnsToGraveyardAndCanBeCastAgain() {
        Card spell = new TheirNumberIsLegion();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player2, List.of(new DenyTheWitch()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromGraveyard(player1, 0);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Their Number Is Legion");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Necron Warrior")).isEmpty();
        harness.assertLife(player1, 20);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertNotInGraveyard(player1, "Their Number Is Legion");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Their Number Is Legion"));
    }
}
