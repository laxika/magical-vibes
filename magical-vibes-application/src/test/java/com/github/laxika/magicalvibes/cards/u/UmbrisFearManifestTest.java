package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HorrorOfTheBrokenLands;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UmbrisFearManifest.class, Forest.class, GrizzlyBears.class, HorrorOfTheBrokenLands.class, Conspiracy.class})
class UmbrisFearManifestTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness count cards opponents own in exile")
    void powerAndToughnessCountOpponentOwnedExiledCards() {
        Permanent umbris = harness.addToBattlefieldAndReturn(player1, new UmbrisFearManifest());
        harness.setExile(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setExile(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, umbris)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, umbris)).isEqualTo(3);
    }

    @Test
    @DisplayName("It exiles each entering Nightmare or Horror's opponent's library cards through a land")
    void qualifyingCreatureEntryExilesUntilLand() {
        Permanent umbris = harness.addToBattlefieldAndReturn(player1, new UmbrisFearManifest());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Forest land = new Forest();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, land, remaining));

        harness.enterBattlefieldAndReturn(player1, new HorrorOfTheBrokenLands());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gqs.getEffectivePower(gd, umbris)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enter trigger cannot target its controller")
    void enterTriggerRequiresOpponentTarget() {
        harness.enterBattlefieldAndReturn(player1, new UmbrisFearManifest());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ownEntryTriggersOnceDespiteHavingBothQualifyingTypes() {
        Forest first = new Forest();
        Forest remaining = new Forest();
        harness.setLibrary(player2, List.of(first, remaining));

        harness.castFromHand(player1, new UmbrisFearManifest(), "{3}{U}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void ownEntryStillTriggersWhenConspiracyReplacesItsCreatureTypes() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(land));

        harness.castFromHand(player1, new UmbrisFearManifest(), "{3}{U}{B}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
    }

    @Test
    void nonqualifyingAndOpponentCreaturesDoNotTrigger() {
        harness.addToBattlefield(player1, new UmbrisFearManifest());
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player2, List.of(top));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new HorrorOfTheBrokenLands());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void noLandExilesTheEntireLibrary() {
        harness.addToBattlefield(player1, new UmbrisFearManifest());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));

        harness.enterBattlefieldAndReturn(player1, new HorrorOfTheBrokenLands());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playersWhoLostGameThisMatch).doesNotContain(player2.getId());
    }

    @Test
    void emptyLibraryDoesNotCauseALossOrExileAnything() {
        harness.addToBattlefield(player1, new UmbrisFearManifest());
        harness.setLibrary(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new HorrorOfTheBrokenLands());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playersWhoLostGameThisMatch).doesNotContain(player2.getId());
    }

    @Test
    void faceDownOpponentOwnedCardsStillCountForTheBoost() {
        Permanent umbris = harness.addToBattlefieldAndReturn(player1, new UmbrisFearManifest());
        gd.addToExile(player2.getId(), new GrizzlyBears(), null, true);
        gd.addToExile(player1.getId(), new GrizzlyBears(), null, true);

        assertThat(gqs.getEffectivePower(gd, umbris)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, umbris)).isEqualTo(2);
    }

    @Test
    void boostUpdatesWhenOpponentOwnedCardsLeaveExile() {
        Permanent umbris = harness.addToBattlefieldAndReturn(player1, new UmbrisFearManifest());
        GrizzlyBears bear = new GrizzlyBears();
        Forest land = new Forest();
        harness.setExile(player2, List.of(bear, land));
        assertThat(gqs.getEffectivePower(gd, umbris)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, umbris)).isEqualTo(3);

        gd.removeFromExile(bear.getId());
        gd.removeFromExile(land.getId());

        assertThat(gqs.getEffectivePower(gd, umbris)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, umbris)).isEqualTo(1);
    }
}
