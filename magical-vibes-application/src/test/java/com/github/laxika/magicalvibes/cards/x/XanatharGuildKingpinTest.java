package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KozilekTheGreatDistortion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VoidmageProdigy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XanatharGuildKingpin.class, Forest.class, GrizzlyBears.class, Shock.class,
        FutureSight.class, VoidmageProdigy.class, KozilekTheGreatDistortion.class, Boomerang.class})
class XanatharGuildKingpinTest extends BaseCardTest {

    private void resolveUpkeepTrigger(Card topCard) {
        harness.setLibrary(player2, List.of(topCard));
        harness.addToBattlefield(player1, new XanatharGuildKingpin());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The controller can cast the target opponent's top spell with any color of mana")
    void castsTargetOpponentsTopSpellWithAnyColorMana() {
        GrizzlyBears bears = new GrizzlyBears();
        resolveUpkeepTrigger(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The controller can play the target opponent's top land")
    void playsTargetOpponentsTopLand() {
        Forest forest = new Forest();
        resolveUpkeepTrigger(forest);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("The chosen opponent can't cast spells until end of turn")
    void chosenOpponentCannotCastSpells() {
        resolveUpkeepTrigger(new Forest());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chosenOpponentCannotCastFromTheirLibrary() {
        resolveUpkeepTrigger(new Shock());
        harness.addToBattlefield(player2, new FutureSight());
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastOpponentsTopCardFaceDown() {
        VoidmageProdigy prodigy = new VoidmageProdigy();
        resolveUpkeepTrigger(prodigy);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        gs.playCardFromLibraryTop(gd, player1, null, null, List.of(), List.of(), true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isFaceDown()
                        && permanent.getOriginalCard().getId().equals(prodigy.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(prodigy);
    }

    @Test
    void coloredManaCannotPayExplicitColorlessCosts() {
        KozilekTheGreatDistortion kozilek = new KozilekTheGreatDistortion();
        resolveUpkeepTrigger(kozilek);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 10);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(kozilek);
    }

    @Test
    void topCardIsVisiblePrivatelyEvenWithoutEnoughMana() {
        GrizzlyBears bears = new GrizzlyBears();
        resolveUpkeepTrigger(bears);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains(bears.getId().toString()));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains(bears.getId().toString()));
    }

    @Test
    void canCastSuccessiveTopCardsAndSpellsKeepTheirOwner() {
        Shock first = new Shock();
        Shock second = new Shock();
        resolveUpkeepTrigger(first);
        harness.setLibrary(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveFromLibraryTop(player1, player2.getId());
        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotCastCreatureDuringUpkeep() {
        GrizzlyBears bears = new GrizzlyBears();
        resolveUpkeepTrigger(bears);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears);
    }

    @Test
    void permissionDoesNotWaiveManaCost() {
        GrizzlyBears bears = new GrizzlyBears();
        resolveUpkeepTrigger(bears);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears);
    }

    @Test
    void cannotPlayASecondLandFromOpponentsLibrary() {
        Forest first = new Forest();
        Forest second = new Forest();
        resolveUpkeepTrigger(first);
        harness.setLibrary(player2, List.of(first, second));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    void permissionsAndSpellLockExpireAtEndOfTurn() {
        Shock topCard = new Shock();
        resolveUpkeepTrigger(topCard);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
        harness.clearMessages();
        harness.publishState();
        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains(topCard.getId().toString()));
    }

    @Test
    void opponentCanRespondBeforeUpkeepAbilityResolves() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new XanatharGuildKingpin());
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvedPermissionSurvivesXanatharLeavingBattlefield() {
        resolveUpkeepTrigger(new Shock());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Xanathar, Guild Kingpin"));
        harness.assertNotOnBattlefield(player1, "Xanathar, Guild Kingpin");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 18);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void playedLandStillBelongsToItsLibraryOwner() {
        Forest forest = new Forest();
        forest.setOwnerId(player2.getId());
        resolveUpkeepTrigger(forest);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromLibraryTop(player1);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Forest"));

        assertThat(gd.playerHands.get(player2.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new XanatharGuildKingpin());
        gd.turnNumber = 2;

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
