package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RestorationAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GloriousProtector.class, GrizzlyBears.class, RestorationAngel.class, Unsummon.class})
class GloriousProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets you exile any number of non-Angel creatures you control")
    void etbExilesChosenNonAngelCreaturesYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new RestorationAngel());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castProtector();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(bears.getId());
        assertThat(choice.validIds()).doesNotContain(angel.getId(), opponentBears.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Restoration Angel");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the battlefield unchanged")
    void canChooseNoCreatures() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castProtector();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled creatures return under their owners' control when Protector leaves")
    void exiledCreaturesReturnWhenProtectorLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castProtector();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        UUID protectorPermanentId = harness.getPermanentId(player1, "Glorious Protector");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, protectorPermanentId);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Glorious Protector");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Foretell exiles Protector face down for later casting")
    void foretellsProtector() {
        GloriousProtector protector = new GloriousProtector();
        harness.setHand(player1, List.of(protector));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(protector.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.foretoldCardIds).contains(protector.getId());
    }

    @Test
    @DisplayName("No creatures are exiled if Protector leaves before its enter ability resolves")
    void doesNotExileAfterProtectorAlreadyLeft() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GloriousProtector(), "{2}{W}{W}");
        harness.passBothPriorities();

        UUID protectorId = harness.getPermanentId(player1, "Glorious Protector");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, protectorId);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        if (choice != null) {
            harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        }

        harness.assertInHand(player1, "Glorious Protector");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Multiple chosen creatures return to their owners rather than their former controller")
    void multipleCreaturesReturnToTheirOwners() {
        GrizzlyBears borrowed = new GrizzlyBears();
        borrowed.setOwnerId(player2.getId());
        Permanent first = harness.addToBattlefieldAndReturn(player1, borrowed);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castProtector();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.findExiledCard(borrowed.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        UUID protectorId = harness.getPermanentId(player1, "Glorious Protector");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, protectorId);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(borrowed.getId()));
        assertThat(gd.findExiledCard(borrowed.getId())).isNull();
        assertThat(gd.findExiledCard(second.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Foretold Protector can be cast with flash on a later opponent's turn for its foretell cost")
    void castsForetoldProtectorOnLaterOpponentsTurn() {
        GloriousProtector protector = new GloriousProtector();
        harness.setHand(player1, List.of(protector));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFromExile(player1, protector.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glorious Protector");
        assertThat(gd.findExiledCard(protector.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Flash does not allow casting Protector on the turn it was foretold")
    void cannotCastOnTurnItWasForetold() {
        GloriousProtector protector = new GloriousProtector();
        harness.setHand(player1, List.of(protector));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, protector.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(protector.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castProtector() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GloriousProtector(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
