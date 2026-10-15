package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TasterOfWares.class, Divination.class, GoblinPiker.class, GrizzlyBears.class,
        HillGiant.class, LightningBolt.class, ActOfTreason.class})
class TasterOfWaresTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    private Permanent castTaster(UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new TasterOfWares())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Taster of Wares");
    }

    @Test
    @DisplayName("The opponent reveals one card per Goblin and the controller exiles one revealed card")
    void revealsGoblinCountAndExilesChosenCard() {
        harness.addToBattlefield(player1, new GoblinPiker());
        Divination divination = new Divination();
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), divination, new HillGiant())));

        Permanent taster = castTaster(player2.getId());

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);

        PendingInteraction.RevealCardsDiscardChoice pick = activeChoice();
        assertThat(pick.revealStage()).isFalse();
        assertThat(pick.decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.getCardsExiledByPermanent(taster.getId()))
                .extracting(Card::getName)
                .containsExactly("Divination");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Hill Giant");
    }

    @Test
    @DisplayName("The controller may cast an exiled instant or sorcery with mana of any type")
    void castsExiledSpellWithAnyManaType() {
        Divination divination = new Divination();
        harness.setHand(player2, new ArrayList<>(List.of(divination, new GrizzlyBears())));
        Permanent taster = castTaster(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.getCardsExiledByPermanent(taster.getId()))
                .extracting(Card::getName)
                .containsExactly("Divination");

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, divination.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Hill Giant");
    }

    @Test
    @DisplayName("A creature exiled this way cannot be cast through Taster of Wares")
    void cannotCastExiledCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(bears, new HillGiant())));
        Permanent taster = castTaster(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(taster.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    @DisplayName("The cast permission ends when Taster of Wares leaves the battlefield")
    void permissionEndsWhenSourceLeaves() {
        Divination divination = new Divination();
        harness.setHand(player2, new ArrayList<>(List.of(divination, new HillGiant())));
        Permanent taster = castTaster(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, new ArrayList<>(List.of(new LightningBolt())));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, taster.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, divination.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    @DisplayName("Taster of Wares can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, new ArrayList<>(List.of(new TasterOfWares())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyHandDoesNotRequireAChoice() {
        harness.setHand(player2, List.of());

        castTaster(player2.getId());

        assertThat(activeChoice()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void revealsEntireHandWhenThereAreMoreGoblinsThanCards() {
        harness.addToBattlefield(player1, new GoblinPiker());
        Divination divination = new Divination();
        harness.setHand(player2, List.of(divination));

        Permanent taster = castTaster(player2.getId());

        assertThat(activeChoice().revealStage()).isFalse();
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(taster.getId())).containsExactly(divination);
    }

    @Test
    void countsGoblinsWhenTheTriggerResolves() {
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        harness.setHand(player2, List.of(new Divination(), new HillGiant()));
        harness.setHand(player1, List.of(new TasterOfWares(), new LightningBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, piker.getId());
        harness.passBothPriorities();

        assertThat(activeChoice().revealStage()).isTrue();
        assertThat(activeChoice().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Goblin Piker");
    }

    @Test
    void exiledInstantCanBeCastDuringTheOpponentsTurnAndGoesToItsOwnersGraveyard() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        castTaster(player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.setLibrary(player2, List.of(new HillGiant(), new HillGiant()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passPriority(player2);

        harness.castFromExile(player1, bolt.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void castingPermissionDoesNotWaiveTheManaCost() {
        Divination divination = new Divination();
        harness.setHand(player2, List.of(divination));
        castTaster(player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, divination.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Divination exileSpellThenTemporarilyLoseControl() {
        Divination divination = new Divination();
        harness.setHand(player2, List.of(divination));
        Permanent taster = castTaster(player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.setLibrary(player1, List.of(new HillGiant(), new HillGiant(), new HillGiant()));
        harness.setLibrary(player2, List.of(new HillGiant(), new HillGiant(), new HillGiant()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player2, 0, taster.getId());
        harness.assertOnBattlefield(player2, "Taster of Wares");
        return divination;
    }

    @Test
    void newControllerDoesNotGainTheOriginalExilePermission() {
        Divination divination = exileSpellThenTemporarilyLoseControl();
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player2, divination.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void regainingControlDoesNotRestartTheExpiredPermission() {
        Divination divination = exileSpellThenTemporarilyLoseControl();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.assertOnBattlefield(player1, "Taster of Wares");
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, divination.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }
}
