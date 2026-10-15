package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FairgroundsWarden.class, GrizzlyBears.class, LightningBolt.class, Unsummon.class})
class FairgroundsWardenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWarden(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns when Fairgrounds Warden dies")
    void exiledCreatureReturnsWhenWardenDies() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWarden(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        killWarden();

        harness.assertNotOnBattlefield(player1, "Fairgrounds Warden");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns when Fairgrounds Warden is bounced")
    void exiledCreatureReturnsWhenWardenBounced() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWarden(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID wardenId = harness.getPermanentId(player1, "Fairgrounds Warden");
        harness.castInstant(player2, 0, wardenId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new FairgroundsWarden()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Warden before its entry ability resolves leaves the target in place")
    void sourceLeavesBeforeExileResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWarden(player2, "Grizzly Bears");
        harness.passBothPriorities();

        killWarden();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fairgrounds Warden");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An entry ability whose target leaves does not exile anything")
    void targetLeavesBeforeExileResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWarden(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fairgrounds Warden");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Warden can enter when no opponent controls a creature")
    void entersWithNoLegalTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new FairgroundsWarden(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fairgrounds Warden");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castWarden(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new FairgroundsWarden()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void killWarden() {
        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID wardenId = harness.getPermanentId(player1, "Fairgrounds Warden");
        harness.castInstant(player2, 0, wardenId);
        harness.passBothPriorities();
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
