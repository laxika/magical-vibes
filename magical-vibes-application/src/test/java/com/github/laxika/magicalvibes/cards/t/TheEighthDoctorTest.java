package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEighthDoctor.class, AncientDen.class, Disenchant.class, Forest.class, MindStone.class})
class TheEighthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("The Eighth Doctor mills three cards when it enters")
    void millsThreeCardsOnEntry() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TheEighthDoctor()));
        addCastMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest", "Forest");
    }

    @Test
    @DisplayName("The graveyard land and permanent permission is shared once per turn")
    void sharesOneHistoricPlayOrCastPerTurn() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setGraveyard(player1, List.of(new MindStone(), new AncientDen(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only historic cards can use The Eighth Doctor's graveyard permission")
    void rejectsNonhistoricGraveyardCards() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A permanent cast through the permission is exiled when it leaves")
    void exilesCastPermanentWhenItLeaves() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setGraveyard(player1, List.of(new MindStone()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        Permanent mindStone = findPermanent(player1, "Mind Stone");

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, mindStone.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Mind Stone");
        harness.assertNotInGraveyard(player1, "Mind Stone");
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
