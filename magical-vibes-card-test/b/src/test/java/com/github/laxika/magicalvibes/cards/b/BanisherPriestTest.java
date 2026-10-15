package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({BanisherPriest.class, GrizzlyBears.class, Shock.class, Unsummon.class})
class BanisherPriestTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPriest(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns when Banisher Priest dies")
    void exiledCreatureReturnsWhenPriestDies() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPriest(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        killPriest();

        harness.assertNotOnBattlefield(player1, "Banisher Priest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns under its owner's control when the priest is bounced")
    void exiledCreatureReturnsWhenPriestBounced() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPriest(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID priestId = harness.getPermanentId(player1, "Banisher Priest");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, priestId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new BanisherPriest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Leaving before the exile trigger resolves prevents exile")
    void leavingBeforeTriggerResolvesPreventsExile() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPriest(player2, "Grizzly Bears");
        harness.passBothPriorities();

        killPriest();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Banisher Priest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The Priest can enter when no opponent controls a creature")
    void entersWithoutLegalTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BanisherPriest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Banisher Priest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exile trigger does nothing if its target leaves before resolution")
    void targetLeavingBeforeResolutionPreventsExile() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castPriest(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Banisher Priest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({Cloudshift.class})
    @DisplayName("A returned Priest cannot extend the duration of its original exile trigger")
    void returnedPriestDoesNotReviveOriginalTrigger() {
        UUID firstBearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID secondBearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        castPriest(player2, "Grizzly Bears");
        harness.passBothPriorities();
        UUID originalPriestId = harness.getPermanentId(player1, "Banisher Priest");

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, originalPriestId);
        harness.handlePermanentChosen(player1, secondBearsId);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, secondBearsId)).isNull();
        assertThat(gqs.findPermanentById(gd, firstBearsId)).isNotNull();
        assertThat(harness.getPermanentId(player1, "Banisher Priest")).isNotEqualTo(originalPriestId);

        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, firstBearsId)).isNotNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    private void castPriest(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new BanisherPriest()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void killPriest() {
        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID priestId = harness.getPermanentId(player1, "Banisher Priest");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, priestId);
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
