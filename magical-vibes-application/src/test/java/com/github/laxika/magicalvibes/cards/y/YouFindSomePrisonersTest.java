package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SpareDagger;
import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouFindSomePrisoners.class, SpareDagger.class, NeverwinterDryad.class, Forest.class})
class YouFindSomePrisonersTest extends BaseCardTest {

    @Test
    void breakTheirChainsDestroysAnArtifact() {
        SpareDagger artifact = new SpareDagger();
        var permanent = harness.addToBattlefieldAndReturn(player2, artifact);
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 0, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spare Dagger");
        harness.assertInGraveyard(player2, "Spare Dagger");
    }

    @Test
    void interrogateThemExilesThreeCardsAndLetsTheChosenCardBeCastWithAnyColor() {
        NeverwinterDryad chosen = new NeverwinterDryad();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player2, List.of(chosen, second, third));
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen, second, third);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(chosen.getId());
        assertThat(gd.exilePlayAnyManaType).contains(chosen.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Neverwinter Dryad");
    }

    @Test
    void breakTheirChainsRequiresAnArtifactTarget() {
        NeverwinterDryad creature = new NeverwinterDryad();
        harness.addToBattlefield(player2, creature);
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void interrogateThemRequiresAnOpponentTarget() {
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void interrogateThemExilesAllRemainingCardsFromAShortLibraryAndOnlyTheChosenCardCanBePlayed() {
        Forest chosen = new Forest();
        Forest other = new Forest();
        harness.setLibrary(player2, List.of(chosen, other));
        castInterrogateThem();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen, other);
        assertThatThrownBy(() -> harness.castFromExile(player1, other.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, chosen.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(other);
    }

    @Test
    void interrogateThemOnAnEmptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player2, List.of());
        castInterrogateThem();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "You Find Some Prisoners");
    }

    @Test
    void chosenLandStillUsesTheNormalLandPlayLimit() {
        Forest chosen = new Forest();
        harness.setLibrary(player2, List.of(chosen));
        castInterrogateThem();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
    }

    @Test
    void choosingACardIsMandatoryButCastingItStillRequiresMana() {
        NeverwinterDryad chosen = new NeverwinterDryad();
        harness.setLibrary(player2, List.of(chosen));
        castInterrogateThem();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Neverwinter Dryad");
    }

    @Test
    void chosenCreatureCannotBeCastDuringAnOpponentsTurnButCanBeCastOnYourNextTurn() {
        NeverwinterDryad chosen = new NeverwinterDryad();
        harness.setLibrary(player2, List.of(chosen, new Forest(), new Forest(), new Forest()));
        castInterrogateThem();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Neverwinter Dryad");
    }

    @Test
    void permissionExpiresAtTheEndOfYourNextTurnAndTheCardStaysExiled() {
        NeverwinterDryad chosen = new NeverwinterDryad();
        harness.setLibrary(player2, List.of(chosen, new Forest(), new Forest(), new Forest(), new Forest()));
        castInterrogateThem();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(chosen.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(chosen);
    }

    private void castInterrogateThem() {
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();
    }
}
