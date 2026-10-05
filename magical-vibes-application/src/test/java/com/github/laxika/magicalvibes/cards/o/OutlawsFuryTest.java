package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RogueSkycaptain;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({OutlawsFury.class, GrizzlyBears.class, RogueSkycaptain.class, Forest.class})
class OutlawsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures +2/+0 until end of turn and does not exile without an outlaw")
    void boostsOwnCreaturesWithoutOutlaw() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castOutlawsFury();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("With an outlaw, exiles the top card and allows playing it through your next turn")
    void boostsAndExilesTopCardWithOutlaw() {
        harness.addToBattlefield(player1, new RogueSkycaptain());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castOutlawsFury();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    void opposingOutlawDoesNotEnableExile() {
        harness.addToBattlefield(player2, new RogueSkycaptain());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castOutlawsFury();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void checksForOutlawAtResolution() {
        harness.setHand(player1, List.of(new OutlawsFury()));
        harness.addMana(player1, ManaColor.RED, 3);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castInstant(player1, 0);
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, outlaw)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outlaw)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void outlawLeavingBeforeResolutionPreventsExile() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new OutlawsFury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(outlaw);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void laterCreaturesDoNotReceiveBoost() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castOutlawsFury();

        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
    }

    @Test
    void emptyLibraryDoesNotPreventBoost() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());
        harness.setLibrary(player1, List.of());

        castOutlawsFury();

        assertThat(gqs.getEffectivePower(gd, outlaw)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Outlaws' Fury");
    }

    @Test
    void exilesOnlyTopCardAndLandStillUsesNormalLandPlay() {
        harness.addToBattlefield(player1, new RogueSkycaptain());
        Card topCard = new Forest();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        castOutlawsFury();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void exiledSpellRequiresManaAndCanBeCastNormally() {
        harness.addToBattlefield(player1, new RogueSkycaptain());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        castOutlawsFury();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void permissionSurvivesOutlawLeavingAndExpiresAfterNextTurn() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castOutlawsFury();
        gd.playerBattlefields.get(player1.getId()).remove(outlaw);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void exiledLandCanBePlayedDuringNextTurn() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castOutlawsFury();
        gd.playerBattlefields.get(player1.getId()).remove(outlaw);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castOutlawsFury() {
        harness.setHand(player1, List.of(new OutlawsFury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0);
    }
}
