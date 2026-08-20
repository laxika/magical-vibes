package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestorationAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        GloriousProtector protector = castProtector();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        UUID protectorPermanentId = harness.getPermanentId(player1, "Glorious Protector");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, protectorPermanentId);
        harness.passBothPriorities();

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

    private GloriousProtector castProtector() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        GloriousProtector protector = new GloriousProtector();
        harness.setHand(player1, List.of(protector));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return protector;
    }
}
