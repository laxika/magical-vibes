package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.k.K9MarkI;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.RoseTyler;
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

@CardUsed({TheEighthDoctor.class, AncientDen.class, Disenchant.class, Forest.class, MindStone.class,
        Humble.class, K9MarkI.class, TrenzaloreClocktower.class, RoseTyler.class, TheCavesOfAndrozani.class})
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
        harness.castAndResolveInstant(player1, 0, mindStone.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Mind Stone");
        harness.assertNotInGraveyard(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Playing a legendary land uses the shared historic permission")
    void legendaryLandUsesSharedPermission() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setGraveyard(player1, List.of(new TrenzaloreClocktower(), new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Trenzalore Clocktower");
        harness.assertNotInGraveyard(player1, "Trenzalore Clocktower");
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A historic land played from the graveyard is exiled when destroyed")
    void exilesPlayedHistoricLand() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setGraveyard(player1, List.of(new AncientDen()));
        prepareMainPhase();
        harness.playGraveyardLand(player1, 0);
        Permanent land = findPermanent(player1, "Ancient Den");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, land.getId());

        harness.assertNotOnBattlefield(player1, "Ancient Den");
        harness.assertNotInGraveyard(player1, "Ancient Den");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("Ancient Den");
    }

    @Test
    @DisplayName("The permission does not allow another land after the normal land play")
    void respectsNormalLandPlayLimit() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new TrenzaloreClocktower()));
        prepareMainPhase();
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Eighth Doctor cannot cast a nonhistoric instant from the graveyard")
    void rejectsNonhistoricSpell() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setGraveyard(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Eighth Doctor mills the whole library if fewer than three cards remain")
    void millsShortLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TheEighthDoctor()));
        addCastMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Losing all abilities removes the exile replacement granted by The Eighth Doctor")
    void abilityLossRemovesGrantedExileReplacement() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setGraveyard(player1, List.of(new K9MarkI()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        Permanent k9 = findPermanent(player1, "K-9, Mark I");
        harness.setHand(player1, List.of(new Humble(), new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, k9.getId());
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, k9.getId());

        harness.assertNotOnBattlefield(player1, "K-9, Mark I");
        harness.assertInGraveyard(player1, "K-9, Mark I");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).doesNotContain("K-9, Mark I");
    }

    @Test
    @DisplayName("A legendary nonartifact creature can be cast from the graveyard only once")
    void castsLegendaryNonartifactCreatureOnce() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setGraveyard(player1, List.of(new RoseTyler(), new MindStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rose Tyler");
        harness.assertNotInGraveyard(player1, "Rose Tyler");
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonlegendary nonartifact Saga can be cast from the graveyard")
    void castsSagaFromGraveyard() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        harness.setGraveyard(player1, List.of(new TheCavesOfAndrozani()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Caves of Androzani");
        harness.assertNotInGraveyard(player1, "The Caves of Androzani");
    }

    @Test
    @DisplayName("The Eighth Doctor cannot play historic lands from an opponent's graveyard")
    void cannotPlayFromOpponentGraveyard() {
        harness.addToBattlefield(player1, new TheEighthDoctor());
        TrenzaloreClocktower land = new TrenzaloreClocktower();
        harness.setGraveyard(player2, List.of(land));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Trenzalore Clocktower");
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
