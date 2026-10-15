package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrapjawTyrant.class, GrizzlyBears.class, Murder.class, Shock.class})
class TrapjawTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("When dealt damage, Trapjaw Tyrant exiles a target creature an opponent controls")
    void damageExilesTargetOpponentCreature() {
        harness.addToBattlefield(player1, new TrapjawTyrant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        shockTrapjaw();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled creature returns when Trapjaw Tyrant leaves the battlefield")
    void exiledCreatureReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player1, new TrapjawTyrant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        shockTrapjaw();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        UUID tyrantId = harness.getPermanentId(player1, "Trapjaw Tyrant");
        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, tyrantId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Trapjaw Tyrant cannot target its controller's creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new TrapjawTyrant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        shockTrapjaw();

        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).doesNotContain(ownBearsId);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Source leaving before the enrage trigger resolves prevents exile")
    void sourceLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new TrapjawTyrant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        shockTrapjaw();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Trapjaw Tyrant"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Trapjaw Tyrant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A target removed in response is not exiled")
    void targetLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new TrapjawTyrant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        shockTrapjaw();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Trapjaw Tyrant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Separate damage events can exile multiple creatures, which all return")
    void separateDamageEventsExileAndReturnAllCreatures() {
        harness.addToBattlefield(player1, new TrapjawTyrant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        shockTrapjaw();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        resetForFollowUpSpell();
        shockTrapjaw();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);

        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Trapjaw Tyrant"));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage with no opponent creatures does not leave a target prompt")
    void noLegalTargetsDoesNotLeavePrompt() {
        harness.addToBattlefield(player1, new TrapjawTyrant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        shockTrapjaw();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Trapjaw Tyrant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void shockTrapjaw() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID tyrantId = harness.getPermanentId(player1, "Trapjaw Tyrant");
        harness.castAndResolveInstant(player2, 0, tyrantId);
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
