package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SkyclaveApparition.class, GrizzlyBears.class, SerraAngel.class, Forest.class, LightningBolt.class})
class SkyclaveApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets only an opposing nonland nontoken permanent with mana value 4 or less")
    void etbExilesOnlyLegalPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player2, new Forest());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID angelId = harness.getPermanentId(player2, "Serra Angel");
        UUID forestId = harness.getPermanentId(player2, "Forest");

        castAndResolveApparition();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bearsId).doesNotContain(angelId, forestId);

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("When it leaves, the exiled card's owner creates a blue Illusion sized by mana value")
    void leavesCreatesTokenForExiledCardOwner() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveApparition();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        UUID apparitionId = harness.getPermanentId(player1, "Skyclave Apparition");
        destroyApparition(apparitionId);
        harness.passBothPriorities();

        Permanent illusion = findPermanent(player2, "Illusion");
        assertThat(illusion.getCard().isToken()).isTrue();
        assertThat(illusion.getEffectivePower()).isEqualTo(2);
        assertThat(illusion.getEffectiveToughness()).isEqualTo(2);
        assertThat(illusion.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(illusion.getCard().getSubtypes()).containsExactly(CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("If no permanent is exiled, leaving creates no Illusion")
    void leavingWithoutExiledCardCreatesNoToken() {
        harness.addToBattlefield(player2, new Forest());
        castAndResolveApparition();

        UUID apparitionId = harness.getPermanentId(player1, "Skyclave Apparition");
        destroyApparition(apparitionId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Illusion"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Illusion"));
    }

    @Test
    @DisplayName("A permanent you control is not a legal target")
    void doesNotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolveApparition();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opposingId).doesNotContain(ownId);
        harness.handlePermanentChosen(player1, opposingId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing the Apparition in response still exiles the target without an Illusion")
    void leavesBeforeExileResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveApparition();
        harness.handlePermanentChosen(player1, bearsId);

        destroyApparition(harness.getPermanentId(player1, "Skyclave Apparition"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyclave Apparition");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Illusion"));
    }

    @Test
    @DisplayName("A target destroyed in response is not exiled and produces no Illusion")
    void targetLeavesBeforeExileResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveApparition();
        harness.handlePermanentChosen(player1, bearsId);
        destroyApparition(bearsId);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        destroyApparition(harness.getPermanentId(player1, "Skyclave Apparition"));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Illusion"));
    }

    @Test
    @DisplayName("The controller can choose zero targets even when a legal target exists")
    void mayDeclineExiling() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveApparition();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).isEmpty();
        destroyApparition(harness.getPermanentId(player1, "Skyclave Apparition"));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Illusion"));
    }

    private void castAndResolveApparition() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SkyclaveApparition(), "{1}{W}{W}");
        harness.passBothPriorities();
    }

    private void destroyApparition(UUID apparitionId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, apparitionId);
    }
}
